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

    private static final Board board = new Board(GamePane.WIDTH, GamePane.HEIGHT);
    private static final int SERVER_PORT = 59001;
    private static final Map<String, Client.ClientState> clientNames = new HashMap<>();
    private static final Set<ObjectOutputStream> outputStreams = new HashSet<>();

    public static final String NAME_REQUEST = "SUBMIT-NAME";
    public static final String NAME_ACCEPT = "NAME-ACCEPTED ";
    public static final String DISCONNECT_REQUEST = "DISCONNECT-CLIENT";
    public static final String CLIENT_STATE_UPDATE = "STATE-UPDATE";

    public static void main(String[] args) throws IOException {
        board.initBoard();

        System.out.println("Server running...");

        var pool = Executors.newFixedThreadPool(10);

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
                            break;
                        }
                    }
                }

                outputStreams.add(outputStream);
                outputStream.writeUTF(NAME_ACCEPT);
                outputStream.writeObject(new Board(board));
                outputStream.flush();


                while (true) {
                    switch (inputStream.readUTF()) {
                        case CLIENT_STATE_UPDATE -> {

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
                }
            }
        }
    }


}
