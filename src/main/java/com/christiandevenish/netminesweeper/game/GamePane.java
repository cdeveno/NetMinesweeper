package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.server.Client;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class GamePane extends BorderPane {

    protected final Client client;
    public static final double WIDTH = 700, HEIGHT = 700;
    protected final Canvas canvas = new Canvas(WIDTH, HEIGHT);
    public final PlayerTable playerTable = new PlayerTable();
    public final Board board;

    private final Thread socketThread;
    private final Mouse mouse = new Mouse(this);

    public GamePane(Client client) {
        this.client = client;
        this.client.setGame(this);
        socketThread = new Thread(this.client);
        socketThread.setDaemon(true);
        socketThread.start();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.client.board != null) break;
        }
        board = new Board(this.client.board);
        board.renderBoard(canvas);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, mouse);
        canvas.setFocusTraversable(true);
        setCenter(canvas);
        setRight(playerTable);
        this.client.setClientState(Client.ClientState.STANDBY);
    }

    public void lostGame() {

    }

    public void wonGame() {

    }
}
