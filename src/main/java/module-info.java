module com.christiandevenish.netminesweeper {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.christiandevenish.netminesweeper to javafx.fxml;
    exports com.christiandevenish.netminesweeper;
    exports com.christiandevenish.netminesweeper.game;
    opens com.christiandevenish.netminesweeper.game to javafx.fxml;
    exports com.christiandevenish.netminesweeper.server;
    opens com.christiandevenish.netminesweeper.server to javafx.fxml;
}