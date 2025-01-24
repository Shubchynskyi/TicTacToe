package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.shubchynskyi.tictactoeapp.strategy.AbstractDifficultyStrategy.FIELD_SIZE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.*;

class TestStrategy extends AbstractDifficultyStrategy {

    @Override
    protected List<StrategyStep> getSteps() {
        return List.of();
    }

    public boolean callTryToWin(Game game) {
        return tryToWin(game);
    }

    public boolean callTryToBlockPlayer(Game game) {
        return tryToBlockPlayer(game);
    }

    public boolean callTryToPutSignInCenter(Game game) {
        return tryToPutSignInCenter(game);
    }

    public boolean callTryToPriorityCell(Game game) {
        return tryToPriorityCell(game);
    }

    public boolean callTryToAvoidCornerTrap(Game game) {
        return tryToAvoidCornerTrap(game);
    }

    public boolean callDoRandomMove(Game game) {
        return doRandomMove(game);
    }

    public boolean callIsPlayerInOppositeCorners(Game game) {
        return isPlayerInOppositeCorners(game);
    }
}

@DisplayName("AbstractDifficultyStrategy Tests with Game mock")
class AbstractDifficultyStrategyTest {

    private TestStrategy strategy;
    private Game mockGame;

    @BeforeEach
    void setUp() {
        strategy = new TestStrategy();
        mockGame = mock(Game.class);
    }

    @Test
    @DisplayName("Should win when a winning cell is found")
    void shouldWinWhenWinningCellIsFound() {
        when(mockGame.getAiSign()).thenReturn(Sign.NOUGHT);
        when(mockGame.getSignAt(anyInt())).thenReturn(Sign.EMPTY);
        AtomicInteger lastSetIndex = new AtomicInteger(-1);
        doAnswer(inv -> {
            int idx = inv.getArgument(0);
            Sign sign = inv.getArgument(1);
            if (sign == Sign.NOUGHT) {
                lastSetIndex.set(idx);
            }
            return null;
        }).when(mockGame).setSignAt(anyInt(), any(Sign.class));
        when(mockGame.isWin(Sign.NOUGHT)).thenAnswer(inv -> lastSetIndex.get() == 5);

        boolean result = strategy.callTryToWin(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(5);
    }

    @Test
    @DisplayName("Should not win if no winning cell is found")
    void shouldNotWinIfNoWinningCellIsFound() {
        when(mockGame.getAiSign()).thenReturn(Sign.NOUGHT);
        when(mockGame.getSignAt(anyInt())).thenReturn(Sign.EMPTY);
        when(mockGame.isWin(Sign.NOUGHT)).thenReturn(false);

        boolean result = strategy.callTryToWin(mockGame);
        assertFalse(result);
        verify(mockGame, never()).doMove(anyInt());
    }

    @Test
    @DisplayName("Should block the player when there is a threat")
    void shouldBlockPlayerWhenThreatDetected() {
        when(mockGame.getPlayerSign()).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(anyInt())).thenReturn(Sign.EMPTY);
        AtomicInteger lastSetIndex = new AtomicInteger(-1);
        doAnswer(inv -> {
            int idx = inv.getArgument(0);
            Sign sign = inv.getArgument(1);
            if (sign == Sign.CROSS) {
                lastSetIndex.set(idx);
            }
            return null;
        }).when(mockGame).setSignAt(anyInt(), any(Sign.class));
        when(mockGame.isWin(Sign.CROSS)).thenAnswer(inv -> lastSetIndex.get() == 2);

        boolean result = strategy.callTryToBlockPlayer(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(2);
    }

    @Test
    @DisplayName("Should not block the player if there is no threat")
    void shouldNotBlockPlayerIfNoThreat() {
        when(mockGame.getPlayerSign()).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(anyInt())).thenReturn(Sign.EMPTY);
        when(mockGame.isWin(Sign.CROSS)).thenReturn(false);

        boolean result = strategy.callTryToBlockPlayer(mockGame);
        assertFalse(result);
        verify(mockGame, never()).doMove(anyInt());
    }

    @Test
    @DisplayName("Should place sign in the center if it is empty")
    void shouldPlaceSignInCenterIfEmpty() {
        when(mockGame.getSignAt(4)).thenReturn(Sign.EMPTY);

        boolean result = strategy.callTryToPutSignInCenter(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(4);
    }

    @Test
    @DisplayName("Should not place sign in the center if it is occupied")
    void shouldNotPlaceSignInCenterIfOccupied() {
        when(mockGame.getSignAt(4)).thenReturn(Sign.CROSS);

        boolean result = strategy.callTryToPutSignInCenter(mockGame);
        assertFalse(result);
        verify(mockGame, never()).doMove(anyInt());
    }

    @Test
    @DisplayName("Should place sign in center or corner on the first move (8 empty cells)")
    void shouldPlaceInCenterOrCornerOnFirstMove() {
        when(mockGame.getSignAt(0)).thenReturn(Sign.CROSS);
        for (int i = 1; i < 9; i++) {
            when(mockGame.getSignAt(i)).thenReturn(Sign.EMPTY);
        }

        boolean result = strategy.callTryToPriorityCell(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(4);
    }

    @Test
    @DisplayName("Should place sign in any available priority cell if not the first move")
    void shouldPlaceInPriorityCellWhenAvailable() {
        when(mockGame.getSignAt(4)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(0)).thenReturn(Sign.NOUGHT);
        for (int i : List.of(1, 2, 3, 5, 6, 7, 8)) {
            when(mockGame.getSignAt(i)).thenReturn(Sign.EMPTY);
        }

        boolean result = strategy.callTryToPriorityCell(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(intThat(arg -> arg == 2 || arg == 6 || arg == 8));
    }

    @Test
    @DisplayName("Should return false if all priority cells are occupied")
    void shouldNotPlaceInPriorityCellIfAllAreOccupied() {
        for (int i : List.of(0, 2, 4, 6, 8)) {
            when(mockGame.getSignAt(i)).thenReturn(Sign.CROSS);
        }
        for (int i : List.of(1, 3, 5, 7)) {
            when(mockGame.getSignAt(i)).thenReturn(Sign.EMPTY);
        }

        boolean result = strategy.callTryToPriorityCell(mockGame);
        assertFalse(result);
        verify(mockGame, never()).doMove(anyInt());
    }

    @Test
    @DisplayName("Should avoid corner trap when conditions are met")
    void shouldAvoidCornerTrapWhenConditionsMet() {
        when(mockGame.getSignAt(0)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(8)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(4)).thenReturn(Sign.NOUGHT);
        for (int i : List.of(1, 2, 3, 5, 6, 7)) {
            when(mockGame.getSignAt(i)).thenReturn(Sign.EMPTY);
        }
        when(mockGame.getPlayerSign()).thenReturn(Sign.CROSS);
        when(mockGame.getAiSign()).thenReturn(Sign.NOUGHT);

        boolean result = strategy.callTryToAvoidCornerTrap(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(intThat(arg -> arg == 1 || arg == 3 || arg == 5 || arg == 7));
    }

    @Test
    @DisplayName("Should return false if corner trap condition is not met")
    void shouldNotAvoidCornerTrapWhenConditionNotMet() {
        when(mockGame.getSignAt(0)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(4)).thenReturn(Sign.NOUGHT);
        for (int i : List.of(1, 2, 3, 5, 6, 7, 8)) {
            when(mockGame.getSignAt(i)).thenReturn(Sign.EMPTY);
        }

        boolean result = strategy.callTryToAvoidCornerTrap(mockGame);
        assertFalse(result);
        verify(mockGame, never()).doMove(anyInt());
    }

    @Test
    @DisplayName("Should make a random move if there are empty cells")
    void shouldMakeRandomMoveWhenEmptyCellsExist() {
        when(mockGame.getSignAt(0)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(1)).thenReturn(Sign.NOUGHT);
        when(mockGame.getSignAt(4)).thenReturn(Sign.NOUGHT);
        when(mockGame.getSignAt(5)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(6)).thenReturn(Sign.NOUGHT);
        for (int i : List.of(2, 3, 7, 8)) {
            when(mockGame.getSignAt(i)).thenReturn(Sign.EMPTY);
        }

        boolean result = strategy.callDoRandomMove(mockGame);
        assertTrue(result);
        verify(mockGame, times(1)).doMove(intThat(arg -> List.of(2, 3, 7, 8).contains(arg)));
    }

    @Test
    @DisplayName("Should return false if there are no empty cells for a random move")
    void shouldNotMakeRandomMoveWhenNoCellsAreEmpty() {
        when(mockGame.getSignAt(anyInt())).thenReturn(Sign.CROSS);

        boolean result = strategy.callDoRandomMove(mockGame);
        assertFalse(result);
        verify(mockGame, never()).doMove(anyInt());
    }

    @Test
    @DisplayName("Should identify when the player is in opposite corners")
    void shouldIdentifyPlayerInOppositeCorners() {
        when(mockGame.getPlayerSign()).thenReturn(Sign.CROSS);

        when(mockGame.getSignAt(0)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(8)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(2)).thenReturn(Sign.EMPTY);
        when(mockGame.getSignAt(6)).thenReturn(Sign.EMPTY);
        assertTrue(strategy.callIsPlayerInOppositeCorners(mockGame));

        when(mockGame.getSignAt(0)).thenReturn(Sign.EMPTY);
        when(mockGame.getSignAt(8)).thenReturn(Sign.EMPTY);
        when(mockGame.getSignAt(2)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(6)).thenReturn(Sign.CROSS);
        assertTrue(strategy.callIsPlayerInOppositeCorners(mockGame));

        when(mockGame.getSignAt(0)).thenReturn(Sign.CROSS);
        when(mockGame.getSignAt(8)).thenReturn(Sign.EMPTY);
        when(mockGame.getSignAt(2)).thenReturn(Sign.EMPTY);
        when(mockGame.getSignAt(6)).thenReturn(Sign.CROSS);
        assertFalse(strategy.callIsPlayerInOppositeCorners(mockGame));
    }

    @Test
    @DisplayName("Should find winning or blocking moves correctly")
    void shouldFindWinningOrBlockingPositions() {
        Sign[] board = new Sign[FIELD_SIZE];
        Arrays.fill(board, Sign.EMPTY);

        when(mockGame.getSignAt(anyInt())).thenAnswer(inv -> board[inv.getArgument(0, Integer.class)]);
        doAnswer(inv -> {
            int idx = inv.getArgument(0);
            Sign sign = inv.getArgument(1);
            board[idx] = sign;
            return null;
        }).when(mockGame).setSignAt(anyInt(), any(Sign.class));
        when(mockGame.isWin(any(Sign.class))).thenAnswer(inv -> {
            Sign s = inv.getArgument(0);
            return (board[0] == s && board[1] == s && board[2] == s) ||
                    (board[3] == s && board[4] == s && board[5] == s) ||
                    (board[6] == s && board[7] == s && board[8] == s);
        });
        when(mockGame.getAiSign()).thenReturn(Sign.NOUGHT);
        when(mockGame.getPlayerSign()).thenReturn(Sign.CROSS);

        board[0] = Sign.NOUGHT;
        board[1] = Sign.NOUGHT;
        boolean result = strategy.callTryToWin(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(2);

        Arrays.fill(board, Sign.EMPTY);
        board[3] = Sign.CROSS;
        board[4] = Sign.CROSS;
        result = strategy.callTryToBlockPlayer(mockGame);
        assertTrue(result);
        verify(mockGame).doMove(5);
    }
}