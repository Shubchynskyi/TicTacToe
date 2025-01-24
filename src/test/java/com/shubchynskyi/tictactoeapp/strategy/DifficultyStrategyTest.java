package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Difficulty Strategies Tests")
class DifficultyStrategyTest {

    @Nested
    @DisplayName("Easy Difficulty Strategy Tests")
    class EasyTests {

        @Test
        @DisplayName("Should make a random move on an empty board")
        void shouldMakeRandomMoveOnEmptyBoard() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.EASY.name());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            long aiCount = Arrays.stream(game.getBoard())
                    .filter(sign -> sign == game.getAiSign())
                    .count();
            assertEquals(1, aiCount);
        }

        @Test
        @DisplayName("Should make a random move on a non-empty board")
        void shouldMakeRandomMoveOnNonEmptyBoard() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.EASY.name());
            game.setSignAt(0, Sign.CROSS);

            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            long aiCount = Arrays.stream(game.getBoard())
                    .filter(sign -> sign == game.getAiSign())
                    .count();
            assertEquals(1, aiCount);
        }
    }

    @Nested
    @DisplayName("Medium Difficulty Strategy Tests")
    class MediumTests {

        @Test
        @DisplayName("Should win immediately when AI can win")
        void shouldWinImmediatelyWhenAiCanWin() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.MEDIUM.name());
            game.setSignAt(0, game.getAiSign());
            game.setSignAt(1, game.getAiSign());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(2));
            assertTrue(game.isWin(game.getAiSign()));
        }

        @Test
        @DisplayName("Should block player when the player can win")
        void shouldBlockPlayerWhenPlayerCanWin() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.MEDIUM.name());
            game.setSignAt(0, Sign.CROSS);
            game.setSignAt(1, Sign.CROSS);
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(2));
            assertFalse(game.isWin(Sign.CROSS));
        }

        @Test
        @DisplayName("Should make a random move when there is no win or threat")
        void shouldMakeRandomMoveWhenNoWinOrThreat() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.MEDIUM.name());
            game.setSignAt(0, Sign.CROSS);
            game.setSignAt(4, game.getAiSign());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            long aiCount = Arrays.stream(game.getBoard())
                    .filter(sign -> sign == game.getAiSign())
                    .count();
            assertEquals(2, aiCount);
        }
    }

    @Nested
    @DisplayName("Hard Difficulty Strategy Tests")
    class HardTests {

        @Test
        @DisplayName("Should win when possible")
        void shouldWinWhenPossible() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.HARD.name());
            game.setSignAt(3, game.getAiSign());
            game.setSignAt(4, game.getAiSign());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(5));
            assertTrue(game.isWin(game.getAiSign()));
        }

        @Test
        @DisplayName("Should block player when player can win")
        void shouldBlockPlayerWhenPlayerCanWin() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.HARD.name());
            game.setSignAt(1, Sign.CROSS);
            game.setSignAt(2, Sign.CROSS);
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(0));
            assertFalse(game.isWin(Sign.CROSS));
        }

        @Test
        @DisplayName("Should take the center if available")
        void shouldTakeCenterIfAvailable() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.HARD.name());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(4));
        }

        @Test
        @DisplayName("Should make a random move when the center is occupied")
        void shouldMakeRandomMoveWhenCenterOccupied() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.HARD.name());
            game.setSignAt(4, Sign.CROSS);
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            long aiCount = Arrays.stream(game.getBoard())
                    .filter(sign -> sign == game.getAiSign())
                    .count();
            assertEquals(1, aiCount);
        }
    }

    @Nested
    @DisplayName("Impossible Difficulty Strategy Tests")
    class ImpossibleTests {

        @Test
        @DisplayName("Should win when possible")
        void shouldWinWhenPossible() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.IMPOSSIBLE.name());
            game.setSignAt(6, game.getAiSign());
            game.setSignAt(7, game.getAiSign());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(8));
            assertTrue(game.isWin(game.getAiSign()));
        }

        @Test
        @DisplayName("Should block player when player can win")
        void shouldBlockPlayerWhenPlayerCanWin() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.IMPOSSIBLE.name());
            game.setSignAt(0, Sign.CROSS);
            game.setSignAt(1, Sign.CROSS);
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(2));
            assertFalse(game.isWin(Sign.CROSS));
        }

        @Test
        @DisplayName("Should avoid corner trap when applicable")
        void shouldAvoidCornerTrapWhenApplicable() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.IMPOSSIBLE.name());
            game.setSignAt(0, Sign.CROSS);
            game.setSignAt(8, Sign.CROSS);
            game.setSignAt(4, game.getAiSign());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            Sign[] board = game.getBoard();
            boolean sideCellTaken = IntStream.of(1, 3, 5, 7)
                    .anyMatch(i -> board[i] == game.getAiSign());
            assertTrue(sideCellTaken);
        }

        @Test
        @DisplayName("Should take a priority cell when no corner trap")
        void shouldTakePriorityCellWhenNoCornerTrap() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.IMPOSSIBLE.name());
            game.setSignAt(0, Sign.CROSS);
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            assertEquals(game.getAiSign(), game.getSignAt(4));
        }

        @Test
        @DisplayName("Should make a random move when all priority cells are occupied")
        void shouldMakeRandomMoveWhenAllPriorityCellsAreOccupied() {
            Game game = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.IMPOSSIBLE.name());
            game.setSignAt(4, Sign.CROSS);
            game.setSignAt(0, Sign.CROSS);
            game.setSignAt(2, game.getAiSign());
            game.setSignAt(6, Sign.CROSS);
            game.setSignAt(8, game.getAiSign());
            game.setCurrentPlayer(game.getAiSign());
            game.getStrategy().makeMove(game);

            long countO = Arrays.stream(game.getBoard())
                    .filter(sign -> sign == game.getAiSign())
                    .count();
            assertEquals(3, countO);
        }
    }
}
