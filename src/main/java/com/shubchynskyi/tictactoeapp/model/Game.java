package com.shubchynskyi.tictactoeapp.model;

import com.shubchynskyi.tictactoeapp.entity.Difficulty;
import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.strategy.*;

import java.util.Arrays;

/**
 * Локальная игра (одиночная или "local" на одном экране).
 * Хранит 3×3 поле, режим (single/local), символ игрока (playerSign), сложность.
 */
public class Game {

    private Sign[] board; // 9 клеток
    private String gameMode; // "single" или "local"
    private Sign playerSign; // Если single, это символ игрока (X/O)
    private String difficulty; // "easy"/"medium"/"hard"/"impossible"

    private Sign currentPlayer; // CROSS(X) или NOUGHT(O)
    private boolean gameOver;
    private String winner; // "X","O","DRAW"

    private DifficultyStrategy strategy;

    public Game() {
        // Пустой конструктор для JSON
        this.board = new Sign[9];
        Arrays.fill(this.board, Sign.EMPTY);
        this.gameMode = "single";
        this.playerSign = Sign.CROSS;
        this.difficulty = "easy";
        this.currentPlayer = Sign.CROSS;
        this.gameOver = false;
        this.winner = null;
        initStrategy();
    }

    public Game(String gameMode, String playerSymbol, String difficulty) {
        this.board = new Sign[9];
        Arrays.fill(this.board, Sign.EMPTY);

        this.gameMode = (gameMode == null) ? "single" : gameMode;

        if (playerSymbol == null || playerSymbol.equalsIgnoreCase("X")) {
            this.playerSign = Sign.CROSS;
        } else {
            this.playerSign = Sign.NOUGHT;
        }

        this.difficulty = (difficulty == null) ? "easy" : difficulty;

        this.currentPlayer = Sign.CROSS; // X ходит первым
        this.gameOver = false;
        this.winner = null;

        initStrategy(); //TODO ?
    }

    private void initStrategy() {
        switch (this.difficulty.toLowerCase()) {
            case "easy":
                this.strategy = new EasyDifficultyStrategy();
                break;
            case "medium":
                this.strategy = new MediumDifficultyStrategy();
                break;
            case "hard":
                this.strategy = new HardDifficultyStrategy();
                break;
            case "impossible":
                this.strategy = new GodDifficultyStrategy();
                break;
            default:
                this.strategy = new EasyDifficultyStrategy();
        }
    }

    // Ход игрока (row,col)
    public void makeMove(int row, int col) {
        if (gameOver) return;
        int idx = row*3 + col;
        if (board[idx] == Sign.EMPTY) {
            board[idx] = currentPlayer;
            checkWinOrDraw();
            if (!gameOver) {
                switchPlayer();
                if ("single".equalsIgnoreCase(gameMode)) {
                    makeAiMoveIfNeeded();
                }
            }
        }
    }

    private void makeAiMoveIfNeeded() {
        if (gameOver) return;
        if (!"single".equalsIgnoreCase(gameMode)) return;
        if (strategy == null) return;

        // AI = противоположный playerSign
        Sign aiSign = (playerSign==Sign.CROSS)? Sign.NOUGHT : Sign.CROSS;
        if (currentPlayer == aiSign) {
            // вызываем стратегию
            strategy.makeMove(this);
            checkWinOrDraw();
            if (!gameOver) {
                switchPlayer();
            }
        }
    }

    // Проверка победы/ничьи
    private void checkWinOrDraw() {
        Sign s = checkWinSign();
        if (s != Sign.EMPTY) {
            gameOver = true;
            winner = (s==Sign.CROSS) ? "X" : "O";
        } else {
            boolean allFilled = true;
            for (Sign sign: board) {
                if (sign==Sign.EMPTY) {
                    allFilled = false;
                    break;
                }
            }
            if (allFilled) {
                gameOver = true;
                winner = "DRAW";
            }
        }
    }

    private Sign checkWinSign() {
        int[][] combos = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] c: combos) {
            if (board[c[0]]!=Sign.EMPTY &&
                    board[c[0]]==board[c[1]] &&
                    board[c[1]]==board[c[2]]) {
                return board[c[0]];
            }
        }
        return Sign.EMPTY;
    }

    private void switchPlayer() {
        if (currentPlayer==Sign.CROSS) currentPlayer = Sign.NOUGHT;
        else currentPlayer = Sign.CROSS;
    }

    // Геттеры/сеттеры

    public Sign[] getBoard() {
        return board;
    }

    // Удобный метод: вернуть 'X' или 'O'
    public String getCurrentPlayer() {
        if (currentPlayer==Sign.CROSS) return "X";
        if (currentPlayer==Sign.NOUGHT) return "O";
        return "";
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getWinner() {
        return winner;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Sign getPlayerSign() {
        return playerSign;
    }

    // для отладки
    public void setBoard(Sign[] board) {
        this.board = board;
    }

    public void doMove(int idx) {
        // Упрощённый метод, AI напрямую вызывает "doMove(cellIndex)"
        if (gameOver) return;
        if (board[idx] == Sign.EMPTY) {
            board[idx] = currentPlayer;
            checkWinOrDraw();
//            if (!gameOver) {
//                switchPlayer();
//            }
        }
    }

    public Sign getSignAt(int index) {
        return board[index];
    }

    public void setSignAt(int index, Sign sign) {
        board[index] = sign;
    }

    public Sign getAiSign() {
        if(playerSign == Sign.CROSS) {
            return Sign.NOUGHT;
        } else return Sign.CROSS;
    }

    public boolean isWin(Sign sign) {
        return checkWinner() == sign;
    }

    private Sign checkWinner() {
        int[][] combos = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] c: combos) {
            if (board[c[0]] != Sign.EMPTY &&
                    board[c[0]] == board[c[1]] &&
                    board[c[1]] == board[c[2]]) {
                return board[c[0]];
            }
        }
        return Sign.EMPTY;
    }
}