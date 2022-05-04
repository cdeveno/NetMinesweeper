package com.christiandevenish.netminesweeper.server;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.christiandevenish.netminesweeper.server.GameServer.*;

public class AdminController {
    public static Client client;
    @FXML
    Button startButton;
    @FXML
    Button restartButton;
    @FXML
    Button kickPlayer;

    @FXML
    public void startGame() {
        startButton.setDisable(true);
        try {
            client.outputStream.writeUTF(GAME_START);
            client.outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void kickPlayer() {
        List<String> names = client.game.playerTable.getNames();
        names.remove(client.name);
        if (names.size() < 1) return;
        ChoiceDialog<String> kickDialog = new ChoiceDialog<>(names.get(0), names);
        kickDialog.setTitle("Kick Player");
        kickDialog.setHeaderText("Select Player to Disconnect");
        kickDialog.setContentText("Use the dropdown menu to select a player and then press OK.");
        Optional<String> result = kickDialog.showAndWait();
        result.ifPresent(s -> client.disconnect(s));
    }

    @FXML
    public void restartGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restart?");
        alert.setHeaderText("Are you sure you want to restart?");
        alert.setContentText("The game won't restart until finished.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get().equals(ButtonType.OK)) {
                try {
                    client.outputStream.writeUTF(GAME_RESTART);
                    client.outputStream.flush();
                    startButton.setDisable(false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}