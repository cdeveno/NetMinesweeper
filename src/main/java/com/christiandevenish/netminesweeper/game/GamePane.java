package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.Main;
import com.christiandevenish.netminesweeper.server.Client;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Objects;

public class GamePane extends BorderPane {

    public final Client client;
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
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        board = this.client.board;
        board.renderBoard(canvas);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, mouse);
        canvas.setFocusTraversable(true);
        setCenter(canvas);
        setRight(playerTable);
        if (client.isAdmin) {
            try {
                setBottom(FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("admin_menu.fxml"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.client.setClientState(Client.ClientState.STANDBY);
    }

    public void lostGame() {

    }

    public void wonGame() {

    }
}
