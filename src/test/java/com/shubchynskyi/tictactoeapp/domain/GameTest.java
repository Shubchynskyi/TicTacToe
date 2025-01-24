package com.shubchynskyi.tictactoeapp.domain;

import com.shubchynskyi.tictactoeapp.TestsConstant;
import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import com.shubchynskyi.tictactoeapp.strategy.EasyDifficultyStrategy;
import com.shubchynskyi.tictactoeapp.strategy.HardDifficultyStrategy;
import com.shubchynskyi.tictactoeapp.strategy.ImpossibleDifficultyStrategy;
import com.shubchynskyi.tictactoeapp.strategy.MediumDifficultyStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game Tests")
class GameTest {

    @Test
    @DisplayName("Should initialize a new game with default settings")
    void shouldInitializeGameWithDefaultSettings() {
        Game game = new Game();
        assertAll(
                () -> assertEquals(TestsConstant.GAME_FIELD_SIZE, game.getBoard().length),
                () -> assertTrue(Arrays.stream(game.getBoard()).allMatch(sign -> sign == Sign.EMPTY)),
                () -> assertEquals(Key.SINGLE_GAME_MOD, game.getGameMode()),
                () -> assertEquals(Sign.CROSS, game.getPlayerSign()),
                () -> assertEquals(Difficulty.EASY, game.getDifficulty()),
                () -> assertEquals(Sign.CROSS.getSign(), game.getCurrentPlayer()),
                () -> assertFalse(game.isGameOver()),
                () -> assertNull(game.getWinner()),
                () -> assertEquals(0, game.getScoreHuman()),
                () -> assertEquals(0, game.getScoreAI()),
                () -> assertNull(game.getWinningCombo()),
                () -> assertNotNull(game.getStrategy()),
                () -> assertInstanceOf(EasyDifficultyStrategy.class, game.getStrategy())
        );
    }

    @Test
    @DisplayName("Should initialize a game with custom settings")
    void shouldInitializeGameWithCustomSettings() {
        Game game = new Game(TestsConstant.MULTIPLAYER_GAME_MODE, TestsConstant.PLAYER_SYMBOL_O, TestsConstant.DIFFICULT_HARD);
        assertAll(
                () -> assertEquals(TestsConstant.GAME_FIELD_SIZE, game.getBoard().length),
                () -> assertTrue(Arrays.stream(game.getBoard()).allMatch(sign -> sign == Sign.EMPTY)),
                () -> assertEquals(TestsConstant.MULTIPLAYER_GAME_MODE, game.getGameMode()),
                () -> assertEquals(Sign.NOUGHT, game.getPlayerSign()),
                () -> assertEquals(Difficulty.HARD, game.getDifficulty()),
                () -> assertEquals(Sign.CROSS.getSign(), game.getCurrentPlayer()),
                () -> assertFalse(game.isGameOver()),
                () -> assertNull(game.getWinner()),
                () -> assertEquals(0, game.getScoreHuman()),
                () -> assertEquals(0, game.getScoreAI()),
                () -> assertNull(game.getWinningCombo()),
                () -> assertNotNull(game.getStrategy()),
                () -> assertInstanceOf(HardDifficultyStrategy.class, game.getStrategy())
        );
    }

    @Test
    @DisplayName("Should make a valid move and switch players in two-player mode")
    void shouldMakeMoveAndSwitchPlayersInTwoPlayerMode() {
        Game game = new Game(TestsConstant.MULTIPLAYER_GAME_MODE, TestsConstant.PLAYER_SYMBOL_X, TestsConstant.DIFFICULT_EASY);
        game.makeMove(0, 0);

        assertAll(
                () -> assertEquals(Sign.CROSS, game.getSignAt(0)),
                () -> assertEquals(Sign.NOUGHT.getSign(), game.getCurrentPlayer()),
                () -> assertFalse(game.isGameOver())
        );

        game.makeMove(1, 1);

        assertAll(
                () -> assertEquals(Sign.NOUGHT, game.getSignAt(4)),
                () -> assertEquals(Sign.CROSS.getSign(), game.getCurrentPlayer()),
                () -> assertFalse(game.isGameOver())
        );
    }

    @Test
    @DisplayName("Should make an AI move after player's move in single-player mode")
    void shouldMakeAiMoveAfterPlayerMoveInSinglePlayerMode() {
        Game game = new Game(Key.SINGLE_GAME_MOD, TestsConstant.PLAYER_SYMBOL_X, TestsConstant.DIFFICULT_EASY);
        game.makeMove(0, 0);

        assertAll(
                () -> assertEquals(Sign.CROSS, game.getSignAt(0)),
                () -> assertNotEquals(Sign.EMPTY, game.getSignAt(0)),
                () -> assertEquals(2, Arrays.stream(game.getBoard()).filter(sign -> sign != Sign.EMPTY).count()),
                () -> assertEquals(Sign.CROSS.getSign(), game.getCurrentPlayer()),
                () -> assertFalse(game.isGameOver())
        );
    }

    @Test
    @DisplayName("Should correctly identify a win condition for horizontal, vertical, and diagonal lines")
    void shouldDetectWinConditions() {
        Game game = new Game();

        // Horizontal
        game.setBoard(new Sign[]{
                Sign.CROSS, Sign.CROSS, Sign.CROSS,
                Sign.EMPTY, Sign.EMPTY, Sign.EMPTY,
                Sign.EMPTY, Sign.EMPTY, Sign.EMPTY
        });
        assertTrue(game.isWin(Sign.CROSS));
        assertArrayEquals(new int[]{0, 1, 2}, game.checkWinCombo());

        // Vertical
        game.setBoard(new Sign[]{
                Sign.NOUGHT, Sign.EMPTY, Sign.EMPTY,
                Sign.NOUGHT, Sign.EMPTY, Sign.EMPTY,
                Sign.NOUGHT, Sign.EMPTY, Sign.EMPTY
        });
        assertTrue(game.isWin(Sign.NOUGHT));
        assertArrayEquals(new int[]{0, 3, 6}, game.checkWinCombo());

        // Diagonal
        game.setBoard(new Sign[]{
                Sign.CROSS, Sign.EMPTY, Sign.EMPTY,
                Sign.EMPTY, Sign.CROSS, Sign.EMPTY,
                Sign.EMPTY, Sign.EMPTY, Sign.CROSS
        });
        assertTrue(game.isWin(Sign.CROSS));
        assertArrayEquals(new int[]{0, 4, 8}, game.checkWinCombo());

        // No win
        game.setBoard(new Sign[]{
                Sign.CROSS, Sign.NOUGHT, Sign.CROSS,
                Sign.NOUGHT, Sign.CROSS, Sign.NOUGHT,
                Sign.NOUGHT, Sign.CROSS, Sign.EMPTY
        });
        assertFalse(game.isWin(Sign.CROSS));
        assertFalse(game.isWin(Sign.NOUGHT));
        assertNull(game.checkWinCombo());
    }

    @Test
    @DisplayName("Should identify a draw condition when the board is full and no winner")
    void shouldDetectDrawWhenBoardIsFull() {
        Game game = new Game();
        Sign[] filledBoard = {
                Sign.CROSS, Sign.NOUGHT, Sign.CROSS,
                Sign.NOUGHT, Sign.CROSS, Sign.NOUGHT,
                Sign.NOUGHT, Sign.EMPTY, Sign.NOUGHT
        };
        game.setBoard(filledBoard);

        game.makeMove(2, 1); // triggers check

        assertAll(
                () -> assertTrue(game.isGameOver()),
                () -> assertEquals(Key.DRAW, game.getWinner()),
                () -> assertNull(game.getWinningCombo()),
                () -> assertEquals(0, game.getScoreHuman()),
                () -> assertEquals(0, game.getScoreAI())
        );
    }

    @Test
    @DisplayName("Should reset the board and maintain scores between rounds")
    void shouldResetBoardAndMaintainScores() {
        Game game = new Game(Key.SINGLE_GAME_MOD, TestsConstant.PLAYER_SYMBOL_X, TestsConstant.DIFFICULT_EASY);

        // Simulate a human win
        game.makeMove(0, 0);
        game.makeMove(0, 1);
        game.makeMove(1, 1);
        game.makeMove(1, 2);
        game.makeMove(2, 2);

        int initialHumanScore = game.getScoreHuman();
        int initialAIScore = game.getScoreAI();

        game.resetBoard();

        assertAll(
                () -> assertTrue(Arrays.stream(game.getBoard()).allMatch(sign -> sign == Sign.EMPTY)),
                () -> assertFalse(game.isGameOver()),
                () -> assertNull(game.getWinner()),
                () -> assertNull(game.getWinningCombo()),
                () -> assertEquals(Sign.CROSS.getSign(), game.getCurrentPlayer()),
                () -> assertEquals(initialHumanScore, game.getScoreHuman()),
                () -> assertEquals(initialAIScore, game.getScoreAI())
        );
    }

    @Test
    @DisplayName("Should switch difficulty levels and apply the correct AI strategy")
    void shouldSwitchDifficultyLevelsAndApplyCorrectAiStrategy() {
        Game game = new Game();
        assertInstanceOf(EasyDifficultyStrategy.class, game.getStrategy());

        game.setDifficulty(Difficulty.MEDIUM);
        game.initStrategy();
        assertInstanceOf(MediumDifficultyStrategy.class, game.getStrategy());

        game.setDifficulty(Difficulty.HARD);
        game.initStrategy();
        assertInstanceOf(HardDifficultyStrategy.class, game.getStrategy());

        game.setDifficulty(Difficulty.IMPOSSIBLE);
        game.initStrategy();
        assertInstanceOf(ImpossibleDifficultyStrategy.class, game.getStrategy());

        game.setDifficulty(Difficulty.EASY);
        game.initStrategy();
        assertInstanceOf(EasyDifficultyStrategy.class, game.getStrategy());
    }

    @Test
    @DisplayName("Should ignore invalid moves and not change the game state")
    void shouldIgnoreInvalidMoves() {
        Game game = new Game();
        game.makeMove(0, 0);
        game.makeMove(1, 1);

        Sign[] initialBoard = game.getBoard().clone();
        String initialCurrentPlayer = game.getCurrentPlayer();
        boolean initialGameOver = game.isGameOver();

        game.makeMove(0, 0); // occupied
        game.makeMove(-1, 0); // out of bounds
        game.makeMove(0, 3);  // out of bounds
        game.makeMove(3, 3);  // out of bounds

        assertAll(
                () -> assertArrayEquals(initialBoard, game.getBoard()),
                () -> assertEquals(initialCurrentPlayer, game.getCurrentPlayer()),
                () -> assertEquals(initialGameOver, game.isGameOver()),
                () -> assertNull(game.getWinner()),
                () -> assertNull(game.getWinningCombo())
        );
    }

    @Test
    @DisplayName("Should calculate and update scores correctly in single-player mode")
    void shouldCalculateScoresInSinglePlayerMode() {
        Game game = new Game(Key.SINGLE_GAME_MOD, TestsConstant.PLAYER_SYMBOL_X, TestsConstant.DIFFICULT_EASY);

        // Human win
        game.setBoard(new Sign[]{
                Sign.CROSS, Sign.CROSS, Sign.CROSS,
                Sign.EMPTY, Sign.NOUGHT, Sign.EMPTY,
                Sign.EMPTY, Sign.NOUGHT, Sign.EMPTY
        });
        game.checkWinOrDraw();
        assertEquals(1, game.getScoreHuman());
        assertEquals(0, game.getScoreAI());

        // AI win
        game.resetBoard();
        game.setBoard(new Sign[]{
                Sign.NOUGHT, Sign.NOUGHT, Sign.NOUGHT,
                Sign.EMPTY, Sign.CROSS, Sign.EMPTY,
                Sign.EMPTY, Sign.CROSS, Sign.EMPTY
        });
        game.checkWinOrDraw();
        assertEquals(1, game.getScoreHuman());
        assertEquals(1, game.getScoreAI());

        // Draw
        game.resetBoard();
        game.setBoard(new Sign[]{
                Sign.CROSS, Sign.NOUGHT, Sign.CROSS,
                Sign.NOUGHT, Sign.CROSS, Sign.NOUGHT,
                Sign.NOUGHT, Sign.CROSS, Sign.NOUGHT
        });
        game.checkWinOrDraw();
        assertEquals(1, game.getScoreHuman());
        assertEquals(1, game.getScoreAI());
    }
}
