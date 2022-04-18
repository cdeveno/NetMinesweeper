package com.christiandevenish.netminesweeper;

import com.christiandevenish.netminesweeper.server.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.util.regex.Pattern;

public class LoginController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField ipField;
    private final String ipMatch = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    @FXML
    private TextField portField;

    @FXML
    private void enterButtonClick() {
        String name = nameField.getText().trim();
        if (name.isBlank() || name.length() < 2 || name.length() > 25) {
            error("name");
            return;
        }
        String ip = ipField.getText().trim();
        if (!Pattern.matches(ipMatch, ip) && !ip.equals("localhost")) {
            error("IP address");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            error("port");
            return;
        }
        Client client = new Client(name, ip, port);
        Main.displayGame(client);
    }

    private void error(String type) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Invalid " + type + " input");
        switch (type) {
            case "name" -> errorAlert.setContentText("Name must be between 2 and 25 characters.");
            case "IP address" -> errorAlert.setContentText("Input must be a valid IP address.");
            case "port" -> errorAlert.setContentText("Port must be a valid number.");
        }
        errorAlert.show();
    }
}