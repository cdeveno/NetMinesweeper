package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.server.Client;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Mouse implements EventHandler<MouseEvent> {

    private final GamePane game;
    private final Board board;
    private boolean firstClick = true;

    public Mouse(GamePane game) {
        this.game = game;
        this.board = game.board;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if (game.client.clientState == Client.ClientState.IN_PROGRESS) {
            Tile tile = board.getTile(mouseEvent.getX(), mouseEvent.getY());
            if (firstClick) {
                board.clearArea(tile);
                board.numberBoard();
                firstClick = false;
            }
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (!tile.isFlagged) {
                    if (tile.isMine) {
                        game.lostGame();
                    } else {
                        if (tile.numberOfMinesInProximity == 0) {
                            board.autoRevealSquares(tile);
                            board.renderBoard(game.canvas);
                        } else {
                            tile.isRevealed = true;
                            tile.render(game.canvas.getGraphicsContext2D());
                        }
                    }
                    if (board.checkWin()) {
                        game.wonGame();
                    }
                }
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                tile.isFlagged = !tile.isFlagged;
                tile.render(game.canvas.getGraphicsContext2D());
            }
        }
    }
}