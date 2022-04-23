package com.christiandevenish.netminesweeper.game;

import javafx.scene.canvas.Canvas;

import java.io.Serializable;
import java.util.Random;

public class Board implements Serializable {

    public final double width, height;
    private final int numTiles = 15;
    private final double tileWidth;
    private final double tileHeight;

    private final Tile[][] board = new Tile[numTiles][numTiles];
    private final float mineDensity = 0.1f;

    public Board(double width, double height) {
        this.width = width;
        this.height = height;
        tileWidth = width / numTiles;
        tileHeight = height / numTiles;
    }

    public void initBoard() {
        Random r = new Random();

        for (int i = 0; i < numTiles; i++) { // row
            for (int j = 0; j < numTiles; j++) { // column
                board[i][j] = new Tile(tileWidth * j, tileHeight * i, tileWidth, tileHeight,
                        i, j, r.nextFloat() < mineDensity);
            }
        }
    }

    public void renderBoard(Canvas canvas) {
        for (int i = 0; i < numTiles; i++) {
            for (int j = 0; j < numTiles; j++) {
                board[i][j].render(canvas.getGraphicsContext2D());
            }
        }
    }

    public void revealMines() {
        for (int i = 0; i < numTiles; i++) {
            for (int j = 0; j < numTiles; j++) {
                if (board[i][j].isMine) board[i][j].isRevealed = true;
            }
        }
    }

    public boolean checkWin() {
        for (Tile[] row : board) {
            for (Tile tile : row) {
                if (!tile.isMine && !tile.isRevealed) return false;
            }
        }
        return true;
    }

    public Tile getTile(double x, double y) {
        int row = (int) Math.floor(y / tileHeight);
        int column = (int) Math.floor(x / tileWidth);
        return board[row][column];
    }

    private int determineMinesInProximity(Tile tile) {
        int minesInProximity = 0;
        if (tile.row != 0) {
            if (board[tile.row - 1][tile.column].isMine) { // top middle
                minesInProximity++;
            }
            if (tile.column + 1 != 1) {
                if (board[tile.row - 1][tile.column - 1].isMine) { // top left
                    minesInProximity++;
                }
            }
            if (tile.column + 1 != numTiles) {
                if (board[tile.row - 1][tile.column + 1].isMine) { // top right
                    minesInProximity++;
                }
            }

        }
        if (tile.row + 1 != numTiles) {
            if (board[tile.row + 1][tile.column].isMine) { // bottom middle
                minesInProximity++;
            }
            if (tile.column != 0) {
                if (board[tile.row + 1][tile.column - 1].isMine) { // bottom left
                    minesInProximity++;
                }
            }
            if (tile.column + 1 != numTiles) {
                if (board[tile.row + 1][tile.column + 1].isMine) { // bottom right
                    minesInProximity++;
                }
            }

        }
        if (tile.column + 1 != numTiles) {
            if (board[tile.row][tile.column + 1].isMine) { // right
                minesInProximity++;
            }
        }
        if (tile.column + 1 != 1) {
            if (board[tile.row][tile.column - 1].isMine) { // left
                minesInProximity++;
            }
        }
        return minesInProximity;
    }

    public void autoRevealSquares(Tile tile) {
        if (!tile.isRevealed) {
            tile.isRevealed = true;
            if (tile.row != 0) {
                if (!board[tile.row - 1][tile.column].isMine) { // top middle
                    if (board[tile.row - 1][tile.column].numberOfMinesInProximity == 0) {
                        autoRevealSquares(board[tile.row - 1][tile.column]);
                    } else {
                        board[tile.row - 1][tile.column].isRevealed = true;
                    }
                }
                if (tile.column + 1 != 1) {
                    if (!board[tile.row - 1][tile.column - 1].isMine) { // top left
                        if (board[tile.row - 1][tile.column - 1].numberOfMinesInProximity == 0) {
                            autoRevealSquares(board[tile.row - 1][tile.column - 1]);
                        } else {
                            board[tile.row - 1][tile.column - 1].isRevealed = true;
                        }
                    }
                }
                if (tile.column + 1 != numTiles) {
                    if (!board[tile.row - 1][tile.column + 1].isMine) { // top right
                        if (board[tile.row - 1][tile.column + 1].numberOfMinesInProximity == 0) {
                            autoRevealSquares(board[tile.row - 1][tile.column + 1]);
                        } else {
                            board[tile.row - 1][tile.column + 1].isRevealed = true;
                        }
                    }
                }

            }
            if (tile.row + 1 != numTiles) {
                if (!board[tile.row + 1][tile.column].isMine) { // bottom middle
                    if (board[tile.row + 1][tile.column].numberOfMinesInProximity == 0) {
                        autoRevealSquares(board[tile.row + 1][tile.column]);
                    } else {
                        board[tile.row + 1][tile.column].isRevealed = true;
                    }
                }
                if (tile.column != 0) {
                    if (!board[tile.row + 1][tile.column - 1].isMine) { // bottom left
                        if (board[tile.row + 1][tile.column - 1].numberOfMinesInProximity == 0) {
                            autoRevealSquares(board[tile.row + 1][tile.column - 1]);
                        } else {
                            board[tile.row + 1][tile.column - 1].isRevealed = true;
                        }
                    }
                }
                if (tile.column + 1 != numTiles) {
                    if (!board[tile.row + 1][tile.column + 1].isMine) { // bottom right
                        if (board[tile.row + 1][tile.column + 1].numberOfMinesInProximity == 0) {
                            autoRevealSquares(board[tile.row + 1][tile.column + 1]);
                        } else {
                            board[tile.row + 1][tile.column + 1].isRevealed = true;
                        }
                    }
                }

            }
            if (tile.column + 1 != numTiles) {
                if (!board[tile.row][tile.column + 1].isMine) { // right
                    if (board[tile.row][tile.column + 1].numberOfMinesInProximity == 0) {
                        autoRevealSquares(board[tile.row][tile.column + 1]);
                    } else {
                        board[tile.row][tile.column + 1].isRevealed = true;
                    }
                }
            }
            if (tile.column + 1 != 1) {
                if (!board[tile.row][tile.column - 1].isMine) { // left
                    if (board[tile.row][tile.column - 1].numberOfMinesInProximity == 0) {
                        autoRevealSquares(board[tile.row][tile.column - 1]);
                    } else {
                        board[tile.row][tile.column - 1].isRevealed = true;
                    }
                }
            }
        }
    }

    public void numberBoard() {
        for (int i = 0; i < numTiles; i++) {
            for (int j = 0; j < numTiles; j++) {
                if (!board[i][j].isMine)
                    board[i][j].numberOfMinesInProximity = this.determineMinesInProximity(board[i][j]);
            }
        }
    }

    public void clearBoard(Canvas canvas) {
        canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
    }

    public void clearArea(Tile tile) {
        if (tile.isMine) {
            tile.isMine = false;
        }
        if (tile.row != 0) {
            if (board[tile.row - 1][tile.column].isMine) { // top middle
                board[tile.row - 1][tile.column].isMine = false;
            }
            if (tile.column + 1 != 1) {
                if (board[tile.row - 1][tile.column - 1].isMine) { // top left
                    board[tile.row - 1][tile.column - 1].isMine = false;
                }
            }
            if (tile.column + 1 != numTiles) {
                if (board[tile.row - 1][tile.column + 1].isMine) { // top right
                    board[tile.row - 1][tile.column + 1].isMine = false;
                }
            }

        }
        if (tile.row + 1 != numTiles) {
            if (board[tile.row + 1][tile.column].isMine) { // bottom middle
                board[tile.row + 1][tile.column].isMine = false;
            }
            if (tile.column != 0) {
                if (board[tile.row + 1][tile.column - 1].isMine) { // bottom left
                    board[tile.row + 1][tile.column - 1].isMine = false;
                }
            }
            if (tile.column + 1 != numTiles) {
                if (board[tile.row + 1][tile.column + 1].isMine) { // bottom right
                    board[tile.row + 1][tile.column + 1].isMine = false;
                }
            }

        }
        if (tile.column + 1 != numTiles) {
            if (board[tile.row][tile.column + 1].isMine) { // right
                board[tile.row][tile.column + 1].isMine = false;
            }
        }
        if (tile.column + 1 != 1) {
            if (board[tile.row][tile.column - 1].isMine) { // left
                board[tile.row][tile.column - 1].isMine = false;
            }
        }
    }
}
