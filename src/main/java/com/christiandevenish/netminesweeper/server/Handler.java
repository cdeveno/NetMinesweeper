package com.christiandevenish.netminesweeper.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.christiandevenish.netminesweeper.server.GameServer.*;

public class Handler implements Runnable {
    private final GameServer server;
    private String name;
    private final Socket socket;
    private ObjectOutputStream outputStream;

    public Handler(GameServer server, Socket socket) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {

        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            while (true) {
                if (!server.serverState.equals(GameServer.ServerState.LOBBY)) break;
                outputStream.writeUTF(NAME_REQUEST);
                outputStream.flush();

                name = inputStream.readUTF();
                if (!server.clientStates.containsKey(name)) {
                    server.clientStates.put(name, Client.ClientState.CONNECTING);
                    server.broadcastMessage(CLIENT_STATE_UPDATE + ":" + name + ":" + server.clientStates.get(name));
                    server.playerTable.add(name, server.clientStates.get(name));
                    server.outputStreams.put(name, outputStream);
                    if (server.clientStates.size() == 1) {
                        outputStream.writeUTF(NAME_ACCEPT + ADMIN_ANNOTATION);
                        server.adminClient = name;
                    } else outputStream.writeUTF(NAME_ACCEPT);
                    break;
                }
            }

            outputStream.writeObject(server.board);
            outputStream.flush();

            synchronized (server.clientStates) {
                for (String name : server.clientStates.keySet()) {
                    if (this.name.equals(name)) continue;
                    outputStream.writeUTF(CLIENT_STATE_UPDATE + ":" + name + ":" + server.clientStates.get(name));
                    outputStream.flush();
                }
            }

            synchronized (server.clientTimes) {
                for (String name : server.clientTimes.keySet()) {
                    if (this.name.equals(name)) continue;
                    outputStream.writeUTF(CLIENT_TIME_UPDATE + ":" + name);
                    outputStream.writeDouble(server.clientTimes.get(name));
                    server.playerTable.editTime(name, server.clientTimes.get(name));
                    outputStream.flush();
                }
            }

            while (!socket.isClosed()) {
                String input = inputStream.readUTF();
                if (input.startsWith(CLIENT_STATE_UPDATE)) {
                    Client.ClientState state = Client.ClientState.valueOf(input.substring(input.indexOf(':') + 1));
                    server.clientStates.put(name, state);
                    server.broadcastMessage(CLIENT_STATE_UPDATE + ":" + name + ":" + server.clientStates.get(name));
                    server.playerTable.add(name, server.clientStates.get(name));
                    server.checkWin();
                } else if (input.startsWith(GAME_START)) {
                    server.serverState = GameServer.ServerState.IN_PROGRESS;
                    server.broadcastMessage(GAME_START);
                } else if (input.startsWith(CLIENT_TIME_UPDATE)) {
                    double time = inputStream.readDouble();
                    server.clientTimes.put(name, time);
                    server.broadcastTimeUpdate(name, time);
                    server.playerTable.editTime(name, server.clientTimes.get(name));
                } else if (input.startsWith(DISCONNECT_REQUEST)) {
                    server.broadcastMessage(input);
                    server.playerTable.remove(input.substring(input.indexOf(':') + 1));
                } else if (input.startsWith(GAME_RESTART)) {
                    if (name.equals(server.adminClient) && server.serverState.equals(ServerState.FINISHED)) {
                        server.restartGame();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (outputStream != null) {
                server.outputStreams.remove(name);
            }
            if (name != null) {
                server.clientStates.remove(name);
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
