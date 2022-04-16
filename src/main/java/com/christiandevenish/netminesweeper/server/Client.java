package com.christiandevenish.netminesweeper.server;

import com.christiandevenish.netminesweeper.game.Board;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Task {

    private final String name, ipAddress;
    private final int port;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    public Board board = null;

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
                if (inputStream.readUTF().startsWith(GameServer.NAME_ACCEPT)) {
                    board = (Board) inputStream.readObject();
                    break;
                }
            }
            while (true) {
                if (inputStream.readUTF().startsWith(GameServer.DISCONNECT_REQUEST)) {
                    socket.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setClientState(ClientState clientState) {
        this.clientState = clientState;
        try {
            outputStream.writeUTF(GameServer.CLIENT_STATE_UPDATE);
            outputStream.writeUTF(clientState.name());
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
}
