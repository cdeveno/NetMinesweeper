package com.christiandevenish.netminesweeper.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.christiandevenish.netminesweeper.server.GameServer.MAX_PLAYERS;
import static com.christiandevenish.netminesweeper.server.GameServer.SERVER_PORT;

public class ServerThread extends Thread {
    ExecutorService pool = Executors.newFixedThreadPool(MAX_PLAYERS);

    private final GameServer server;

    public ServerThread(GameServer server) {
        this.server = server;

    }

    @Override
    public void run() {
        server.board.initBoard();
        try (ServerSocket listener = new ServerSocket(SERVER_PORT)) {
            while (!pool.isShutdown()) {
                pool.execute(new Handler(server, listener.accept()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
