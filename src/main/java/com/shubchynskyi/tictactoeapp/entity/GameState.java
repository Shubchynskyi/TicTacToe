package com.shubchynskyi.tictactoeapp.entity;

import java.util.Arrays;

public class GameState {
    private GameMode gameMode;
    private String difficulty;
    private String playerSign;
    private String aiSign;
    private String[][] board;
    private boolean gameOver;
    private String winner;

    public GameState(GameMode gameMode, String difficulty, String sign) {
        this.gameMode = gameMode;
        this.difficulty = difficulty;
        this.playerSign = sign;
        this.aiSign = sign.equals("cross") ? "nought" : "cross";
        this.board = new String[3][3];
        for (String[] row : board) {
            Arrays.fill(row, "");
        }
        this.gameOver = false;
        this.winner = null;
    }

    public boolean makeMove(int row, int col, String sign) {
        if (board[row - 1][col - 1].isEmpty()) {
            board[row - 1][col - 1] = sign;
            return true;
        }
        return false;
    }

    public boolean checkWin(String sign) {
        // Проверка строк, столбцов и диагоналей
        for (int i = 0; i < 3; i++) {
            if (board[i][0].equals(sign) && board[i][1].equals(sign) && board[i][2].equals(sign)) {
                winner = sign;
                return true;
            }
            if (board[0][i].equals(sign) && board[1][i].equals(sign) && board[2][i].equals(sign)) {
                winner = sign;
                return true;
            }
        }
        if (board[0][0].equals(sign) && board[1][1].equals(sign) && board[2][2].equals(sign)) {
            winner = sign;
            return true;
        }
        if (board[0][2].equals(sign) && board[1][1].equals(sign) && board[2][0].equals(sign)) {
            winner = sign;
            return true;
        }
        return false;
    }

    public boolean isDraw() {
        for (String[] row : board) {
            for (String cell : row) {
                if (cell.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    // Геттеры и сеттеры
    public GameMode getGameMode() {
        return gameMode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getPlayerSign() {
        return playerSign;
    }

    public String getAiSign() {
        return aiSign;
    }

    public String[][] getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}