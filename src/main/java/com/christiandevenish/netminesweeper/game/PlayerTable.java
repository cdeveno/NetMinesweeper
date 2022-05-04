package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.server.Client;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerTable extends TableView<PlayerTable.Player> {

    final ObservableList<Player> data = FXCollections.observableArrayList();

    public PlayerTable() {
        setEditable(true);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Player, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<Player, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> data.getValue().timeProperty());

        TableColumn<Player, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());

        setItems(data);
        getColumns().addAll(nameCol, timeCol, statusCol);
    }

    public static class Player {
        private final StringProperty name;
        private StringProperty time;
        private StringProperty status;

         public Player(String name, double time, String status) {
             this.name = new SimpleStringProperty(name);
             this.time = new SimpleStringProperty(String.format("%.2f", time));
             this.status = new SimpleStringProperty(status);
         }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getTime() {
            return time.get();
        }

        public StringProperty timeProperty() {
            return time;
        }

        public void setTime(String time) {
            this.time.set(time);
        }

        public String getStatus() {
            return status.get();
        }

        public StringProperty statusProperty() {
            return status;
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }

    public void add(String name, Client.ClientState status) {
        for (Player player : data) {
            if (player.getName().equals(name)) {
                player.setStatus(status.name());
                refresh();
                return;
            }
        }
        data.add(new Player(name, 0.0, status.name()));
    }

    public void editTime(String name, double time) {
        for (Player player : data) {
            if (player.getName().equals(name)) {
                player.setTime(String.format("%.2f", time));
                break;
            }
        }
    }

    public void clearTimes() {
        for (Player player : data) {
            player.setTime(Double.toString(0.0));
        }
    }

    public void remove(String name) {
        Iterator<Player> iterator = data.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getName().equals(name)) {
                iterator.remove();
                break;
            }
        }
    }
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Player player : data) {
            names.add(player.getName());
        }
        return names;
    }

    public void showWinners() {
        data.sort((o1, o2) -> {
            if (o1.getStatus().equals(Client.ClientState.WON.name())
                    && !o2.getStatus().equals(Client.ClientState.WON.name())) {
                return -1;
            } else {
                return Double.compare(Double.parseDouble(o2.getTime()),
                        Double.parseDouble(o1.getTime()));
            }
        });
    }
}
