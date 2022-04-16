package com.christiandevenish.netminesweeper;

import com.christiandevenish.netminesweeper.game.GamePane;
import com.christiandevenish.netminesweeper.server.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage stage;
    private static GamePane game;

    @Override
    public void start(Stage stage) throws IOException {
        Main.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 600);

        stage.setTitle("Login");
        stage.setScene(scene);

        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void displayGame(Client client) {
        game = new GamePane(client);
        stage.setTitle("NetMinesweeper");
        stage.setScene(new Scene(game));
    }
}