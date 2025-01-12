package com.shubchynskyi.tictactoeapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import com.shubchynskyi.tictactoeapp.strategy.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;


@Getter
@Setter
public class Game {

    private Sign[] board;      // 9 клеток: 0..8
    private String gameMode;   // "single" или "local"
    private Sign playerSign;   // Если single, это символ игрока (X/O)
    private String difficulty; // "easy"/"medium"/"hard"/"impossible"
    private Sign currentPlayer;
    private boolean gameOver;
    private String winner; // "X","O","DRAW"
    private int scoreHuman;
    private int scoreAI;
    private int[] winningCombo;

    @JsonIgnore
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

        // "single" или "local"
        this.gameMode = (gameMode == null) ? "single" : gameMode;

        // X или O
        if (playerSymbol == null || playerSymbol.equalsIgnoreCase("X")) {
            this.playerSign = Sign.CROSS;
        } else {
            this.playerSign = Sign.NOUGHT;
        }

        // easy/medium/hard/impossible
        this.difficulty = (difficulty == null) ? "easy" : difficulty;

        this.currentPlayer = Sign.CROSS;
        this.gameOver = false;
        this.winner = null;

        initStrategy();

        if ("single".equalsIgnoreCase(this.gameMode)) {
            Sign aiSign = (this.playerSign == Sign.CROSS) ? Sign.NOUGHT : Sign.CROSS;
            if (aiSign == Sign.CROSS) {
                makeAiMoveIfNeeded();
            }
        }
    }

    private void initStrategy() {
        String d = this.difficulty.toLowerCase();
        switch (d) {
            case "medium":
                this.strategy = new MediumDifficultyStrategy();
                break;
            case "hard":
                this.strategy = new HardDifficultyStrategy();
                break;
            case "impossible":
                this.strategy = new ImpossibleDifficultyStrategy();
                break;
            default:
                this.strategy = new EasyDifficultyStrategy();
        }
    }

    public void makeMove(int row, int col) {
        if (gameOver) return;
        int idx = row * 3 + col;
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

        Sign aiSign = (playerSign == Sign.CROSS) ? Sign.NOUGHT : Sign.CROSS;
        if (currentPlayer == aiSign) {
            strategy.makeMove(this);
            checkWinOrDraw();
            if (!gameOver) {
                switchPlayer();
            }
        }
    }

    private void switchPlayer() {
        if (currentPlayer == Sign.CROSS) currentPlayer = Sign.NOUGHT;
        else currentPlayer = Sign.CROSS;
    }

    private void checkWinOrDraw() {
        if (gameOver) {
            return;
        }
        Sign wSign = checkWinSign();
        if (wSign != Sign.EMPTY) {
            gameOver = true;

            if ("single".equalsIgnoreCase(gameMode)) {
                if (wSign == playerSign) {
                    scoreHuman++;
                } else {
                    scoreAI++;
                }
            }

            if (wSign == Sign.CROSS) {
                winner = "X";
            } else {
                winner = "O";
            }
            this.winningCombo=checkWinCombo();
        } else {
            // check if board is full
            boolean allFilled = true;
            for (Sign s : board) {
                if (s == Sign.EMPTY) {
                    allFilled = false;
                    break;
                }
            }
            if (allFilled) {
                gameOver = true;
                winner = "DRAW";
                this.winningCombo=null;
            }
        }
    }

    private Sign checkWinSign() {
        int[][] combos = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] c : combos) {
            if (board[c[0]] != Sign.EMPTY &&
                    board[c[0]] == board[c[1]] &&
                    board[c[1]] == board[c[2]]) {
                return board[c[0]];
            }
        }
        return Sign.EMPTY;
    }

    // Дополнительно
    public int[] checkWinCombo() {
        int[][] combos = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] c : combos) {
            if (board[c[0]]!=Sign.EMPTY
                    && board[c[0]]==board[c[1]]
                    && board[c[1]]==board[c[2]]) {
                return c;
            }
        }
        return null;
    }

    // Возвращаем "X" или "O"
    public String getCurrentPlayer() {
        if (currentPlayer == Sign.CROSS) return "X";
        if (currentPlayer == Sign.NOUGHT) return "O";
        return "";
    }

    // AI-стратегия может напрямую делать ход:
    public void doMove(int idx) {
        if (!gameOver && board[idx] == Sign.EMPTY) {
            board[idx] = currentPlayer;
            checkWinOrDraw();
        }
    }

    public void resetBoard() {
        Arrays.fill(this.board, Sign.EMPTY);
        this.gameOver = false;
        this.winner = null;
        this.winningCombo=null;
        this.currentPlayer = Sign.CROSS; // default

        if ("single".equalsIgnoreCase(gameMode)) {
            // if AI= X => AI ходит...
            Sign aiSign = (playerSign == Sign.CROSS) ? Sign.NOUGHT : Sign.CROSS;
            if (aiSign == Sign.CROSS) {
                makeAiMoveIfNeeded();
            }
        }
    }

    public Sign getSignAt(int i) {
        return board[i];
    }

    public void setSignAt(int i, Sign sign) {
        board[i] = sign;
    }

    public boolean isWin(Sign sign) {
        return (checkWinSign() == sign);
    }

    public Sign getAiSign() {
        // если человек=playerSign=X => AI=O
        return (playerSign == Sign.CROSS) ? Sign.NOUGHT : Sign.CROSS;
    }

}