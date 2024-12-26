package com.shubchynskyi.tictactoeapp.model;

public class Game {
    private String[][] board;
    private String gameMode;
    private String playerSymbol;
    private String difficulty;
    private String currentPlayer;
    private boolean gameOver;
    private String winner;

    public Game() {}

    public Game(String gameMode, String playerSymbol, String difficulty) {
        this.board = new String[3][3];
        this.gameMode = gameMode;
        this.playerSymbol = (playerSymbol == null) ? "X" : playerSymbol;
        this.difficulty = (difficulty == null) ? "1" : difficulty;
        this.currentPlayer = "X";
        this.gameOver = false;
        this.winner = null;
    }

    // Getters and setters

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getPlayerSymbol() {
        return playerSymbol;
    }

    public void setPlayerSymbol(String playerSymbol) {
        this.playerSymbol = playerSymbol;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
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


    public void makeMove(int row, int col) {
        if (gameOver) return;
        if (board[row][col] == null) {
            board[row][col] = currentPlayer;
            checkWinOrDraw();
            if (!gameOver) {
                switchPlayer();
            }
        }
    }

    public void makeAiMoveIfNeeded() {
        if (!gameOver && "single".equals(gameMode)) {
            String aiSymbol = playerSymbol.equals("X") ? "O" : "X";
            if (currentPlayer.equals(aiSymbol)) {
                java.util.Random rand = new java.util.Random();
                while (!gameOver) {
                    int r = rand.nextInt(3);
                    int c = rand.nextInt(3);
                    if (board[r][c] == null) {
                        makeMove(r, c);
                        break;
                    }
                }
            }
        }
    }

    // Switch to the other player
    private void switchPlayer() {
        if (currentPlayer.equals("X")) {
            currentPlayer = "O";
        } else {
            currentPlayer = "X";
        }
    }

    // Check if someone has won or if it's a draw
    private void checkWinOrDraw() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1])
                    && board[i][1].equals(board[i][2])) {
                gameOver = true;
                winner = board[i][0];
                return;
            }
        }
        // Check columns
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != null && board[0][j].equals(board[1][j])
                    && board[1][j].equals(board[2][j])) {
                gameOver = true;
                winner = board[0][j];
                return;
            }
        }
        // Check diagonals
        if (board[0][0] != null && board[0][0].equals(board[1][1])
                && board[1][1].equals(board[2][2])) {
            gameOver = true;
            winner = board[0][0];
            return;
        }
        if (board[0][2] != null && board[0][2].equals(board[1][1])
                && board[1][1].equals(board[2][0])) {
            gameOver = true;
            winner = board[0][2];
            return;
        }

        // Check for draw (if all cells are filled)
        boolean allFilled = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) {
                    allFilled = false;
                    break;
                }
            }
        }
        if (allFilled) {
            gameOver = true;
            winner = "DRAW";
        }
    }
}
