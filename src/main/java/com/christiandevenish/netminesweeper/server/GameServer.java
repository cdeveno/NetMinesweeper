package com.christiandevenish.netminesweeper.server;

import com.christiandevenish.netminesweeper.Main;
import com.christiandevenish.netminesweeper.game.Board;
import com.christiandevenish.netminesweeper.game.GamePane;
import com.christiandevenish.netminesweeper.game.PlayerTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer extends Application {
    Board board = new Board(GamePane.WIDTH, GamePane.HEIGHT);

    @FXML
    Button startButton;
    @FXML
    Button stopButton;
    @FXML
    PlayerTable playerTable;
    @FXML
    Circle serverStatus;

    private final ServerThread serverThread = new ServerThread(this);
    public static final int MAX_PLAYERS = 25;
    public static final int SERVER_PORT = 59001;
    ServerState serverState = ServerState.LOBBY;
    final Map<String, Client.ClientState> clientStates = new ConcurrentHashMap<>();
    final Map<String, Double> clientTimes = new ConcurrentHashMap<>();
    final Map<String, ObjectOutputStream> outputStreams = new ConcurrentHashMap<>();
    String adminClient;

    public static final String NAME_REQUEST = "SUBMIT-NAME";
    public static final String NAME_ACCEPT = "NAME-ACCEPTED ";
    public static final String ADMIN_ANNOTATION = "-isAdmin";
    public static final String CLIENT_STATE_UPDATE = "STATE-UPDATE";
    public static final String DISCONNECT_REQUEST = "DISCONNECT-CLIENT";
    public static final String GAME_START = "GAME_START";
    public static final String GAME_FINISH = "GAME_FINISH";
    public static final String GAME_RESTART = "GAME_RESTART";
    public static final String CLIENT_TIME_UPDATE = "TIME_UPDATE";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("server_menu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Game Server");
        stage.setScene(scene);
        stage.setResizable(false);
        Platform.setImplicitExit(false);
        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Click the stop button to stop the server!");
            alert.setHeaderText("Stop Button");
            alert.show();
            event.consume();
        });
        stage.show();
    }

    void broadcastTimeUpdate(String name, double time) throws IOException {
        synchronized (outputStreams) {
            for (ObjectOutputStream o : outputStreams.values()) {
                o.writeUTF(CLIENT_TIME_UPDATE + ":" + name);
                o.writeDouble(time);
                o.flush();
            }
        }
    }

    void broadcastMessage(String message) throws IOException {
        synchronized (outputStreams) {
            for (ObjectOutputStream o : outputStreams.values()) {
                o.writeUTF(message);
                o.flush();
            }
        }
    }

    void checkWin() throws IOException {
        synchronized (clientStates) {
            for (Client.ClientState state : clientStates.values()) {
                if (!state.equals(Client.ClientState.WON)
                        && !state.equals(Client.ClientState.LOST)) {
                    return;
                }
            }
            serverState = ServerState.FINISHED;
            broadcastMessage(GAME_FINISH);
        }
    }

    protected void restartGame() throws IOException {
        playerTable.clearTimes();
        serverState = ServerState.LOBBY;
        board = new Board(GamePane.WIDTH, GamePane.HEIGHT);
        board.initBoard();
        synchronized (outputStreams) {
            for (String name : outputStreams.keySet()) {
                outputStreams.get(name).writeUTF(GAME_RESTART);
                outputStreams.get(name).writeObject(board);
                outputStreams.get(name).flush();
            }
        }
    }

    @FXML
    public void startGame() {
        serverThread.setDaemon(true);
        serverThread.start();
        startButton.setDisable(true);
        serverStatus.setFill(Color.LIMEGREEN);
    }

    public void stopGame() throws IOException {
        synchronized (outputStreams) {
            for (String name : outputStreams.keySet()) {
                outputStreams.get(name).writeUTF(DISCONNECT_REQUEST + ":" + name);
                outputStreams.get(name).flush();
            }
        }
        serverThread.pool.shutdownNow();
        Platform.exit();
    }

    public enum ServerState {
        LOBBY,
        IN_PROGRESS,
        FINISHED
    }
}
