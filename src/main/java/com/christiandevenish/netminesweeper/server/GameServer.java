package com.christiandevenish.netminesweeper.server;

import com.christiandevenish.netminesweeper.game.Board;
import com.christiandevenish.netminesweeper.game.GamePane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class GameServer {

    private static ServerState serverState = ServerState.LOBBY;
    private static final Board board = new Board(GamePane.WIDTH, GamePane.HEIGHT);
    private static final int MAX_PLAYERS = 25;
    private static final int SERVER_PORT = 59001;
    private static final Map<String, Client.ClientState> clientNames = new HashMap<>();
    private static final Map<String, Double> clientTimes = new HashMap<>();
    private static final Set<ObjectOutputStream> outputStreams = new HashSet<>();

    public static final String NAME_REQUEST = "SUBMIT-NAME";
    public static final String NAME_ACCEPT = "NAME-ACCEPTED ";
    public static final String ADMIN_ANNOTATION = "-isAdmin";
    public static final String CLIENT_STATE_UPDATE = "STATE-UPDATE";
    public static final String DISCONNECT_REQUEST = "DISCONNECT-CLIENT";
    public static final String GAME_START = "GAME_START";
    public static final String CLIENT_TIME_UPDATE = "TIME_UPDATE";

    public static void main(String[] args) throws IOException {
        board.initBoard();

        var pool = Executors.newFixedThreadPool(MAX_PLAYERS);

        try (ServerSocket listener = new ServerSocket(SERVER_PORT)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {
        private String name;
        private final Socket socket;
        private ObjectOutputStream outputStream;
        private ObjectInputStream inputStream;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    outputStream.writeUTF(NAME_REQUEST);
                    outputStream.flush();

                    name = inputStream.readUTF();
                    synchronized (clientNames) {
                        if (!clientNames.containsKey(name)) {
                            clientNames.put(name, Client.ClientState.CONNECTING);
                            broadcastStatusUpdate(name, Client.ClientState.CONNECTING);
                            break;
                        }
                    }
                }

                outputStreams.add(outputStream);
                synchronized (clientNames) {
                    if (clientNames.size() == 1) outputStream.writeUTF(NAME_ACCEPT + ADMIN_ANNOTATION);
                    else outputStream.writeUTF(NAME_ACCEPT);
                }
                outputStream.writeObject(board);
                outputStream.flush();


                synchronized (clientNames) {
                    for (String name : clientNames.keySet()) {
                        if (this.name.equals(name)) continue;
                        outputStream.writeUTF(CLIENT_STATE_UPDATE + ":" + name + ":" + clientNames.get(name));
                        outputStream.flush();
                    }
                }

                synchronized (clientTimes) {
                    for (String name : clientTimes.keySet()) {
                        if (this.name.equals(name)) continue;
                        outputStream.writeUTF(CLIENT_TIME_UPDATE + ":" + name);
                        outputStream.writeDouble(clientTimes.get(name));
                        outputStream.flush();
                    }
                }

                while (!socket.isClosed()) {
                    String input = inputStream.readUTF();
                    if (input.startsWith(CLIENT_STATE_UPDATE)) {
                        Client.ClientState state = Client.ClientState.valueOf(input.substring(input.indexOf(':') + 1));
                        synchronized (clientNames) {
                            clientNames.put(name, state);
                        }
                        broadcastStatusUpdate(name, state);
                    } else if (input.startsWith(GAME_START)) {
                        serverState = ServerState.IN_PROGRESS;
                        broadcastMessage(GAME_START);
                    } else if (input.startsWith(CLIENT_TIME_UPDATE)) {
                        double time = inputStream.readDouble();
                        clientTimes.put(name, time);
                        broadcastTimeUpdate(name, time);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                if (outputStream != null) {
                    outputStreams.remove(outputStream);
                }
                if (name != null) {
                    clientNames.remove(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private static void broadcastStatusUpdate(String name, Client.ClientState state) throws IOException {
        synchronized (outputStreams) {
            for (ObjectOutputStream o : outputStreams) {
                o.writeUTF(CLIENT_STATE_UPDATE + ":" + name + ":" + state.name());
                o.flush();
            }
        }
    }

    private static void broadcastTimeUpdate(String name, double time) throws IOException {
        synchronized (outputStreams) {
            for (ObjectOutputStream o : outputStreams) {
                o.writeUTF(CLIENT_TIME_UPDATE + ":" + name);
                o.writeDouble(time);
                o.flush();
            }
        }
    }

    private static void broadcastMessage(String message) throws IOException {
        synchronized (outputStreams) {
            for (ObjectOutputStream o : outputStreams) {
                o.writeUTF(message);
                o.flush();
            }
        }
    }

    enum ServerState {
        LOBBY,
        IN_PROGRESS,
        FINISHED
    }
}
