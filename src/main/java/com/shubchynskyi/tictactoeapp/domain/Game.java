package com.shubchynskyi.tictactoeapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import com.shubchynskyi.tictactoeapp.strategy.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Game {

    private Sign[] board;
    private String gameMode;
    private Sign playerSign;
    private Difficulty difficulty;

    private Sign currentPlayer;
    private boolean gameOver;
    private String winner;
    private int scoreHuman;
    private int scoreAI;
    private int[] winningCombo;

    @JsonIgnore
    private DifficultyStrategy strategy;

    public Game() {
        board = new Sign[9];
        Arrays.fill(board, Sign.EMPTY);
        gameMode = Key.SINGLE_GAME_MOD;
        playerSign = Sign.CROSS;
        difficulty = Difficulty.EASY;
        currentPlayer = Sign.CROSS;
        gameOver = false;
        winner = null;
        initStrategy();
    }

    public Game(String gameMode, String playerSymbol, String difficulty) {
        board = new Sign[9];
        Arrays.fill(board, Sign.EMPTY);

        this.gameMode = (gameMode == null) ? Key.SINGLE_GAME_MOD : gameMode;

        if (playerSymbol == null || playerSymbol.equalsIgnoreCase(Sign.CROSS.getSign())) {
            playerSign = Sign.CROSS;
        } else {
            playerSign = Sign.NOUGHT;
        }

        this.difficulty = (difficulty == null) ? Difficulty.EASY : Difficulty.valueOf(difficulty.toUpperCase());

        currentPlayer = Sign.CROSS;
        gameOver = false;
        winner = null;
        initStrategy();

        if (Key.SINGLE_GAME_MOD.equalsIgnoreCase(gameMode)) {
            Sign aiSign = (playerSign == Sign.CROSS) ? Sign.NOUGHT : Sign.CROSS;
            if (aiSign == Sign.CROSS) {
                makeAiMoveIfNeeded();
            }
        }
    }

    void initStrategy() {
        switch (difficulty) {
            case MEDIUM:
                strategy = new MediumDifficultyStrategy();
                break;
            case HARD:
                strategy = new HardDifficultyStrategy();
                break;
            case IMPOSSIBLE:
                strategy = new ImpossibleDifficultyStrategy();
                break;
            default:
                strategy = new EasyDifficultyStrategy();
        }
    }

    public void makeMove(int row, int col) {
        if (gameOver) return;
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            return;
        }
        int idx = row * 3 + col;
        if (board[idx] == Sign.EMPTY) {
            board[idx] = currentPlayer;
            checkWinOrDraw();
            if (!gameOver) {
                switchPlayer();
                if (Key.SINGLE_GAME_MOD.equalsIgnoreCase(gameMode)) {
                    makeAiMoveIfNeeded();
                }
            }
        }
    }

    private void makeAiMoveIfNeeded() {
        if (gameOver || !Key.SINGLE_GAME_MOD.equalsIgnoreCase(gameMode) || strategy == null) return;

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
        currentPlayer = (currentPlayer == Sign.CROSS) ? Sign.NOUGHT : Sign.CROSS;
    }

    void checkWinOrDraw() {
        if (gameOver) return;

        Sign wSign = checkWinSign();
        if (wSign != Sign.EMPTY) {
            gameOver = true;
            if (Key.SINGLE_GAME_MOD.equalsIgnoreCase(gameMode)) {
                if (wSign == playerSign) {
                    scoreHuman++;
                } else {
                    scoreAI++;
                }
            }
            winner = wSign.getSign();
            winningCombo = checkWinCombo();
        } else {
            boolean allFilled = true;
            for (Sign s : board) {
                if (s == Sign.EMPTY) {
                    allFilled = false;
                    break;
                }
            }
            if (allFilled) {
                gameOver = true;
                winner = Key.DRAW;
                winningCombo = null;
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

    public int[] checkWinCombo() {
        int[][] combos = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] c : combos) {
            if (board[c[0]] != Sign.EMPTY &&
                    board[c[0]] == board[c[1]] &&
                    board[c[1]] == board[c[2]]) {
                return c;
            }
        }
        return null;
    }

    public String getCurrentPlayer() {
        return currentPlayer.getSign();
    }

    public void doMove(int idx) {
        if (!gameOver && idx >= 0 && idx < 9 && board[idx] == Sign.EMPTY) {
            board[idx] = currentPlayer;
            checkWinOrDraw();
        }
    }

    public void resetBoard() {
        Arrays.fill(board, Sign.EMPTY);
        gameOver = false;
        winner = null;
        winningCombo = null;
        currentPlayer = Sign.CROSS;

        if (Key.SINGLE_GAME_MOD.equalsIgnoreCase(gameMode)) {
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
        return (playerSign == Sign.CROSS) ? Sign.NOUGHT : Sign.CROSS;
    }
}