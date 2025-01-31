package com.shubchynskyi.tictactoeapp.service;

import com.shubchynskyi.tictactoeapp.TestsConstant;
import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.WebSocketCommand;
import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.domain.OnlineGame;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("OnlineGameService Tests")
class OnlineGameServiceTest {

    private final OnlineGameService onlineGameService = new OnlineGameService();
    private final ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

    @BeforeEach
    void overrideScheduler() {
        ReflectionTestUtils.setField(onlineGameService, TestsConstant.SCHEDULER, mockScheduler);
    }

    @Test
    @DisplayName("Should create a new game with a unique ID")
    void shouldCreateGameWithUniqueId() {
        long gameId1 = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        long gameId2 = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);

        assertTrue(gameId1 > 1000);
        assertTrue(gameId2 > 1000);
        assertNotEquals(gameId1, gameId2);

        OnlineGame g1 = onlineGameService.getOnlineGame(gameId1);
        OnlineGame g2 = onlineGameService.getOnlineGame(gameId2);

        assertNotNull(g1);
        assertNotNull(g2);

        assertEquals(TestsConstant.FIRST_USER_ID, g1.getCreatorId());
        assertEquals(TestsConstant.FIRST_USER_NAME, g1.getCreatorDisplay());
        assertEquals(TestsConstant.FIRST_USER_ID, g2.getCreatorId());
        assertEquals(TestsConstant.FIRST_USER_NAME, g2.getCreatorDisplay());
    }

    @Test
    @DisplayName("Should return the correct game or null if not found")
    void shouldReturnGameOrNull() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        OnlineGame existing = onlineGameService.getOnlineGame(gameId);
        assertNotNull(existing);

        OnlineGame missing = onlineGameService.getOnlineGame(9999L);
        assertNull(missing);
    }

    @Test
    @DisplayName("Should list all existing games")
    void shouldListAllGames() {
        List<OnlineGame> before = onlineGameService.listGames();
        int initialSize = before.size();

        onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        onlineGameService.createGame(TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);

        List<OnlineGame> after = onlineGameService.listGames();
        assertEquals(initialSize + 2, after.size());
    }

    @Test
    @DisplayName("Should handle a winning move and update game state")
    void shouldHandleWinningMove() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        onlineGameService.joinGame(gameId, TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);

        Game game = onlineGame.getGame();
        game.setBoard(new Sign[]{
                Sign.EMPTY, Sign.CROSS, Sign.CROSS,
                Sign.EMPTY, Sign.NOUGHT, Sign.NOUGHT,
                Sign.EMPTY, Sign.EMPTY, Sign.EMPTY
        });

        // X makes winning move at index 0
        onlineGameService.makeMove(gameId, TestsConstant.FIRST_USER_ID, 0, 0);
        // user2 tries to move in the same cell
        onlineGameService.makeMove(gameId, TestsConstant.SECOND_USER_ID, 0, 0);

        assertTrue(onlineGame.isFinished());
        assertTrue(game.isGameOver());
        assertNotEquals(onlineGame.getScoreX(), onlineGame.getScoreO());
    }

    @Test
    @DisplayName("Should not allow a move if it's not the current player's turn")
    void shouldNotAllowMoveIfNotPlayersTurn() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        onlineGameService.joinGame(gameId, TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);

        onlineGameService.makeMove(gameId, onlineGame.getPlayerOId(), 0, 0);
        assertEquals(Sign.EMPTY, onlineGame.getGame().getSignAt(0));
        assertEquals(Sign.CROSS.getSign(), onlineGame.getGame().getCurrentPlayer());
    }

    @Test
    @DisplayName("Should not make a move if game is not found or already finished")
    void shouldNotMakeMoveIfGameNotFoundOrFinished() {
        // 1) Game not found
        onlineGameService.makeMove(TestsConstant.INCORRECT_GAME_ID, TestsConstant.FIRST_USER_ID, 0, 0);

        // 2) Create and immediately finish the game
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        onlineGame.setFinished(true);

        onlineGameService.makeMove(gameId, TestsConstant.SECOND_USER_ID, 0, 0);
        assertTrue(onlineGame.isFinished());
        assertEquals(Sign.EMPTY, onlineGame.getGame().getSignAt(0));
    }

    @Test
    @DisplayName("Should handle a draw scenario in handleGameOver()")
    void shouldHandleDrawScenario() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        onlineGameService.joinGame(gameId, TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        Game game = onlineGame.getGame();
        game.setBoard(new Sign[]{
                Sign.EMPTY, Sign.NOUGHT, Sign.CROSS,
                Sign.CROSS, Sign.CROSS, Sign.NOUGHT,
                Sign.NOUGHT, Sign.CROSS, Sign.NOUGHT
        });

        onlineGameService.makeMove(gameId, TestsConstant.FIRST_USER_ID, 0, 0);
        onlineGameService.makeMove(gameId, TestsConstant.SECOND_USER_ID, 0, 0);

        assertTrue(onlineGame.isFinished());
        assertEquals(Key.DRAW, onlineGame.getWinnerDisplay());
        assertEquals(0, onlineGame.getScoreX());
        assertEquals(0, onlineGame.getScoreO());
    }

    @Test
    @DisplayName("Should allow a second player to join if the game is waiting")
    void shouldJoinSecondPlayer() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        OnlineGame game = onlineGameService.getOnlineGame(gameId);

        assertTrue(game.isWaitingForSecondPlayer());

        onlineGameService.joinGame(gameId, TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);
        assertFalse(game.isWaitingForSecondPlayer());

        boolean secondIsX = TestsConstant.SECOND_USER_ID.equals(game.getPlayerXId());
        boolean secondIsO = TestsConstant.SECOND_USER_ID.equals(game.getPlayerOId());
        assertTrue(secondIsX || secondIsO);
    }

    @Test
    @DisplayName("Should remove game from map if creator leaves")
    void shouldRemoveGameIfCreatorLeaves() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        assertNotNull(onlineGameService.getOnlineGame(gameId));

        boolean removed = onlineGameService.leaveGame(gameId, TestsConstant.FIRST_USER_ID);
        assertTrue(removed);
        assertNull(onlineGameService.getOnlineGame(gameId));
    }

    @Test
    @DisplayName("Should reset the game but not remove it if non-creator leaves mid-game")
    void shouldResetGameWhenNonCreatorLeavesMidGame() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        onlineGameService.joinGame(gameId, TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);

        onlineGame.getGame().makeMove(0, 0);
        boolean removed = onlineGameService.leaveGame(gameId, TestsConstant.SECOND_USER_ID);
        assertFalse(removed);

        assertTrue(onlineGame.isWaitingForSecondPlayer());
        assertTrue(Arrays.stream(onlineGame.getGame().getBoard()).allMatch(s -> s == Sign.EMPTY));
        assertFalse(onlineGame.isFinished());
        assertEquals(0, onlineGame.getScoreX());
        assertEquals(0, onlineGame.getScoreO());
    }

    @Test
    @DisplayName("Should rematch and switch players/scores correctly")
    void shouldRematchAndSwitchPlayersAndScores() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        onlineGameService.joinGame(gameId, TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);
        OnlineGame game = onlineGameService.getOnlineGame(gameId);

        game.setScoreX(2);
        game.setScoreO(1);
        game.setFinished(true);

        OnlineGame rematched = onlineGameService.rematchGame(gameId);
        assertNotNull(rematched);
        assertNotEquals(rematched.getScoreX(), rematched.getScoreO());
        assertFalse(rematched.isFinished());
        assertFalse(rematched.isWaitingForSecondPlayer());
    }

    @Test
    @DisplayName("Should plan timers and stop them on stopTimerForGame()")
    void shouldPlanTimersAndStopOnStopTimerForGame() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        assertFalse(onlineGameService.getGameTimers().containsKey(gameId));

        onlineGameService.startInactivityTimer(gameId, messagingTemplate, 1);
        assertTrue(onlineGameService.getGameTimers().containsKey(gameId));

        onlineGameService.stopTimerForGame(gameId);
        assertFalse(onlineGameService.getGameTimers().containsKey(gameId));
    }

    @Test
    @DisplayName("Should do nothing if startInactivityTimer() is called for a non-existing or finished game")
    void shouldNotStartInactivityTimerIfGameNotFoundOrFinished() {
        onlineGameService.startInactivityTimer(TestsConstant.INCORRECT_GAME_ID, messagingTemplate, 1);
        assertFalse(onlineGameService.getGameTimers().containsKey(TestsConstant.INCORRECT_GAME_ID));

        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        OnlineGame g = onlineGameService.getOnlineGame(gameId);
        g.setFinished(true);

        onlineGameService.startInactivityTimer(gameId, messagingTemplate, 1);
        assertFalse(onlineGameService.getGameTimers().containsKey(gameId));
    }

    @Test
    @DisplayName("Should send warning at 30s and close at 60s without sleeping")
    void shouldSendWarningAndCloseOnInactivity() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        assertNotNull(onlineGame);

        ArgumentCaptor<Runnable> warningCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> closeCaptor = ArgumentCaptor.forClass(Runnable.class);

        ScheduledFuture<?> mockWarningFuture = mock(ScheduledFuture.class);
        ScheduledFuture<?> mockCloseFuture = mock(ScheduledFuture.class);

        when(mockScheduler.schedule(warningCaptor.capture(), eq(30L), eq(TimeUnit.SECONDS)))
                .thenReturn((ScheduledFuture) mockWarningFuture);
        when(mockScheduler.schedule(closeCaptor.capture(), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn((ScheduledFuture) mockCloseFuture);

        onlineGameService.startInactivityTimer(gameId, messagingTemplate, 1);

        assertTrue(onlineGameService.getGameTimers().containsKey(gameId));

        warningCaptor.getValue().run();
        verify(messagingTemplate).convertAndSend(
                Route.TOPIC_ONLINE_GAME_PREFIX + gameId,
                WebSocketCommand.TIME_LEFT_30
        );
        assertFalse(onlineGame.isFinished());

        closeCaptor.getValue().run();
        verify(messagingTemplate).convertAndSend(
                Route.TOPIC_ONLINE_GAME_PREFIX + gameId,
                WebSocketCommand.CLOSED
        );
        verify(messagingTemplate).convertAndSend(eq(Route.TOPIC_GAME_LIST), any(Object.class));

        assertFalse(onlineGameService.getGames().containsKey(gameId));
        assertFalse(onlineGameService.getGameTimers().containsKey(gameId));
    }

    @Test
    @DisplayName("Should handle a full timer scenario without sleeping (create -> join -> leave -> remove)")
    void shouldHandleFullScenarioTimersWithoutSleep() {
        long gameId = onlineGameService.createGame(TestsConstant.FIRST_USER_ID, TestsConstant.FIRST_USER_NAME);
        assertNotNull(onlineGameService.getOnlineGame(gameId));

        onlineGameService.startInactivityTimer(gameId, messagingTemplate, 5);
        assertTrue(onlineGameService.getGameTimers().containsKey(gameId));

        onlineGameService.joinGame(gameId, TestsConstant.SECOND_USER_ID, TestsConstant.SECOND_USER_NAME);
        onlineGameService.startInactivityTimer(gameId, messagingTemplate, 5);
        assertTrue(onlineGameService.getGameTimers().containsKey(gameId));

        onlineGameService.leaveGame(gameId, TestsConstant.SECOND_USER_ID);
        onlineGameService.startInactivityTimer(gameId, messagingTemplate, 5);
        assertTrue(onlineGameService.getGames().containsKey(gameId));
        assertTrue(onlineGameService.getGameTimers().containsKey(gameId));

        boolean removed = onlineGameService.leaveGame(gameId, TestsConstant.FIRST_USER_ID);
        assertTrue(removed);

        assertNull(onlineGameService.getOnlineGame(gameId));
        assertFalse(onlineGameService.getGameTimers().containsKey(gameId));
    }
}
