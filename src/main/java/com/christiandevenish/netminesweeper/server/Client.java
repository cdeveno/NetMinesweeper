package com.christiandevenish.netminesweeper.server;

import com.christiandevenish.netminesweeper.game.Board;
import com.christiandevenish.netminesweeper.game.GamePane;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.christiandevenish.netminesweeper.server.GameServer.*;

public class Client extends Task<Void> {

    final String name;
    private final String ipAddress;
    private final int port;
    public boolean isAdmin = false;
    ObjectOutputStream outputStream;
    public Board board = null;
    GamePane game;

    private ClientState clientState; // SHOULD ONLY BE CHANGED FROM SETTER!

    public Client(String name, String ipAddress, int port) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        clientState = ClientState.CONNECTING;
    }

    @Override
    public Void call() {
        try {
            Socket socket = new Socket(ipAddress, port);

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            while (true) {
                if (inputStream.readUTF().startsWith(NAME_REQUEST)) {
                    outputStream.writeUTF(name);
                    outputStream.flush();
                    break;
                }
            }
            while (true) {
                String input = inputStream.readUTF();
                if (input.startsWith(NAME_ACCEPT)) {
                    if (input.contains(ADMIN_ANNOTATION)) isAdmin = true;
                    board = (Board) inputStream.readObject();
                    synchronized (game) {
                        game.notifyAll();
                    }
                    break;
                }
            }

            while (!socket.isClosed()) {
                String input = inputStream.readUTF();
                if (input.startsWith(CLIENT_STATE_UPDATE)) {
                    String name = input.substring(input.indexOf(':') + 1, input.lastIndexOf(':'));
                    ClientState status = ClientState.valueOf(input.substring(input.lastIndexOf(':') + 1));
                    game.playerTable.add(name, status);
                } else if (input.startsWith(CLIENT_TIME_UPDATE)) {
                    String name = input.substring(input.indexOf(':') + 1);
                    double time = inputStream.readDouble();
                    game.playerTable.editTime(name, time);
                } else if (input.startsWith(GAME_START)) {
                    setClientState(ClientState.IN_PROGRESS);
                    game.startGame();
                } else if (input.startsWith(DISCONNECT_REQUEST)) {
                    String name = input.substring(input.indexOf(':') + 1);
                    if (this.name.equals(name)) {
                        Platform.exit();
                    } else {
                        game.playerTable.remove(name);
                    }
                } else if (input.startsWith(GAME_FINISH)) {
                    game.playerTable.showWinners();
                } else if (input.startsWith(GAME_RESTART)) {
                    game.playerTable.clearTimes();
                    board = (Board) inputStream.readObject();
                    game.setBoard(board);
                    setClientState(ClientState.STANDBY);
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
            outputStream.writeUTF(CLIENT_STATE_UPDATE + ":" + clientState.name());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTime(double time) {
        try {
            outputStream.writeUTF(CLIENT_TIME_UPDATE);
            outputStream.writeDouble(time);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(String name) {
        try {
            outputStream.writeUTF(DISCONNECT_REQUEST + ":" + name);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public String getName() {
        return name;
    }
}
