package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.server.Client;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class GamePane extends BorderPane {

    protected final Client client;
    public static final double WIDTH = 700, HEIGHT = 700;
    protected final Canvas canvas = new Canvas(GamePane.WIDTH, GamePane.HEIGHT);
    public final Board board;

    private final Thread socketThread;
    private final Mouse mouse = new Mouse(this);

    public GamePane(Client client) {
        this.client = client;
        socketThread = new Thread(client);
        socketThread.setDaemon(true);
        socketThread.start();
        while (client.board == null) System.out.println("Waiting for board...");
        board = new Board(client.board);
        board.renderBoard(canvas);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, mouse);
        canvas.setFocusTraversable(true);
        setCenter(canvas);
        client.setClientState(Client.ClientState.STANDBY);
    }

    public void lostGame() {

    }

    public void wonGame() {

    }
}
