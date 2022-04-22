package com.christiandevenish.netminesweeper.game;

import com.christiandevenish.netminesweeper.server.Client;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Mouse implements EventHandler<MouseEvent> {

    private final GamePane game;
    private boolean firstClick = true;

    public Mouse(GamePane game) {
        this.game = game;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if (game.client.getClientState() == Client.ClientState.IN_PROGRESS) {
            Tile tile = game.board.getTile(mouseEvent.getX(), mouseEvent.getY());
            if (firstClick) {
                game.board.clearArea(tile);
                game.board.numberBoard();
                firstClick = false;
            }
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (!tile.isFlagged) {
                    if (tile.isMine) {
                        game.lostGame();
                    } else {
                        if (tile.numberOfMinesInProximity == 0) {
                            game.board.autoRevealSquares(tile);
                            game.board.renderBoard(game.canvas);
                        } else {
                            tile.isRevealed = true;
                            tile.render(game.canvas.getGraphicsContext2D());
                        }
                    }
                    if (game.board.checkWin()) {
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