package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.server.Client;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;

public class Timer extends Task<Double> {
    public SimpleDoubleProperty timeElapsed  = new SimpleDoubleProperty(0.0);
    private final Client client;

    public Timer(Client client) {
        this.client = client;
    }


    @Override
    protected Double call() {
        long initTime = System.nanoTime();

        while (client.getClientState().equals(Client.ClientState.IN_PROGRESS)) {
            long nowTime = System.nanoTime();
            double delta = (nowTime - initTime) / 1_000_000_000.0;
            if (delta - timeElapsed.get() >= 0.1) {
                setTimeElapsed(delta);
            }
        }

        return getTimeElapsed();
    }

    public double getTimeElapsed() {
        return timeElapsed.get();
    }

    public SimpleDoubleProperty timeElapsedProperty() {
        return timeElapsed;
    }

    public void setTimeElapsed(double timeElapsed) {
        this.timeElapsed.set(timeElapsed);
    }
}
