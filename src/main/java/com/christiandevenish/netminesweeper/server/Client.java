package com.christiandevenish.netminesweeper.server;

import com.christiandevenish.netminesweeper.game.Board;
import com.christiandevenish.netminesweeper.game.GamePane;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Task<Void> {

    private final String name, ipAddress;
    private final int port;
    public boolean isAdmin = false;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    public Board board = null;
    private GamePane game;

    private ClientState clientState;

    public Client(String name, String ipAddress, int port) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        clientState = ClientState.CONNECTING;
    }

    @Override
    public Void call() {
        try {
            socket = new Socket(ipAddress, port);

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            while (true) {
                if (inputStream.readUTF().startsWith(GameServer.NAME_REQUEST)) {
                    outputStream.writeUTF(name);
                    outputStream.flush();
                    break;
                }
            }
            while (true) {
                String input = inputStream.readUTF();
                if (input.startsWith(GameServer.NAME_ACCEPT)) {
                    if (input.contains(GameServer.ADMIN_ANNOTATION)) isAdmin = true;
                    board = (Board) inputStream.readObject();
                    break;
                }
            }

            while (!socket.isClosed()) {
                String input = inputStream.readUTF();
                if (input.startsWith(GameServer.CLIENT_STATE_UPDATE)) {
                    String name = input.substring(input.indexOf(':') + 1, input.lastIndexOf(':'));
                    ClientState status = ClientState.valueOf(input.substring(input.lastIndexOf(':') + 1));
                    game.playerTable.add(name, status);
                    System.out.println("Adding " + name + " with status " + status.name());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ClientState getClientState() {
        return clientState;
    }

    public void setClientState(ClientState clientState) {
        this.clientState = clientState;
        try {
            outputStream.writeUTF(GameServer.CLIENT_STATE_UPDATE + ":" + clientState.name());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum ClientState {
        CONNECTING,
        STANDBY,
        WON,
        LOST,
        IN_PROGRESS
    }

    public void setGame(GamePane game) {
        this.game = game;
    }
}
