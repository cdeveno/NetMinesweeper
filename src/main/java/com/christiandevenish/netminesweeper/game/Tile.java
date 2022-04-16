package com.christiandevenish.netminesweeper.game;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.Serializable;

public class Tile implements Serializable {

    public final double width, height;
    public int row, column;
    public final double x, y;

    public boolean isMine;
    public boolean isRevealed = false;
    public boolean isFlagged = false;

    public int numberOfMinesInProximity = -1;

    public Tile(double x, double y, double width, double height, int row, int column, boolean isMine) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.row = row;
        this.column = column;
        this.isMine = isMine;
    }

    public void render(GraphicsContext gc) {
        gc.clearRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, width, height);
        if (isRevealed) {
            if (isMine) {
                gc.setFill(Color.RED);
                gc.fillRect(x, y, width, height);
                gc.setFill(Color.BLACK);
                gc.fillOval(x, y, width, height);
            } else {
                if (numberOfMinesInProximity != 0) {
                    gc.setFill(Color.GREEN);
                    gc.setFont(new Font(15));
                    gc.fillText("" + numberOfMinesInProximity, x + (width * 0.5), y + (height * 0.5));
                }
            }
        } else {
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y, width, height);
            if (isFlagged) {
                gc.setFill(Color.RED);
                gc.fillRect(x + (width * 0.25), y + (height * 0.25), width / 2, height / 2);
            }
        }
    }
}
