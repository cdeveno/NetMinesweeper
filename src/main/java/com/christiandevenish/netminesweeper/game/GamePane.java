package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.Main;
import com.christiandevenish.netminesweeper.server.AdminController;
import com.christiandevenish.netminesweeper.server.Client;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Objects;

public class GamePane extends BorderPane {

    public final Client client;
    public static final double WIDTH = 700, HEIGHT = 700;
    protected final Canvas canvas = new Canvas(WIDTH, HEIGHT);
    public final PlayerTable playerTable = new PlayerTable();
    protected Board board;
    private final Mouse mouse;
    private final Timer timer;
    private Thread timerThread;
    public GamePane(Client client) {
        this.client = client;
        this.client.setGame(this);
        Thread socketThread = new Thread(this.client);
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
        mouse = new Mouse(this);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, mouse);
        canvas.setFocusTraversable(true);
        setCenter(canvas);
        setRight(playerTable);

        timer = new Timer(this.client);
        Text timerText = new Text();
        timerText.setFont(new Font("Arial", 15));
        timerText.textProperty().bindBidirectional(timer.timeElapsed,
                new DecimalFormat("##0.0"));
        setTop(timerText);
        setAlignment(timerText, Pos.TOP_CENTER);
        timerThread = new Thread(timer);
        timerThread.setDaemon(true);
        timerThread.setPriority(Thread.MIN_PRIORITY);

        if (client.isAdmin) {
            try {
                AdminController.client = this.client;
                setBottom(FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("admin_menu.fxml"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.client.setClientState(Client.ClientState.STANDBY);
    }

    public void lostGame() {
        client.setClientState(Client.ClientState.LOST);
        board.revealMines();
        client.sendTime(timer.getTimeElapsed());
    }

    public void wonGame() {
        client.setClientState(Client.ClientState.WON);
        client.sendTime(timer.getTimeElapsed());
    }

    public void startGame() {
        mouse.firstClick = true;
        timerThread = new Thread(timer);
        timerThread.setDaemon(true);
        timerThread.setPriority(Thread.MIN_PRIORITY);
        timerThread.start();
    }

    public void setBoard(Board board) {
        this.board = board;
        this.board.renderBoard(canvas);
        timer.setTimeElapsed(0.0);
    }
}
