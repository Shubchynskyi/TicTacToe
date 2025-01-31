package com.shubchynskyi.tictactoeapp.service;

import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.WebSocketCommand;
import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.domain.OnlineGame;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OnlineGameService {

    @Getter
    private final ConcurrentHashMap<Long, OnlineGame> games = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1000);

    @Getter
    private final Map<Long, TimerHandles> gameTimers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public long createGame(String userId, String displayName) {
        long gameId = idGen.incrementAndGet();
        OnlineGame onlineGame = new OnlineGame(gameId, userId, displayName);
        games.put(gameId, onlineGame);
        return gameId;
    }

    public OnlineGame getOnlineGame(long gameId) {
        return games.get(gameId);
    }

    public List<OnlineGame> listGames() {
        return new ArrayList<>(games.values());
    }

    public void joinGame(long gameId, String userId, String displayName) {
        OnlineGame onlineGame = games.get(gameId);
        if (onlineGame != null && onlineGame.isWaitingForSecondPlayer()) {
            assignPlayer(onlineGame, userId, displayName);
            onlineGame.setWaitingForSecondPlayer(false);
        }
    }

    public void makeMove(long gameId, String userId, int row, int col) {
        OnlineGame onlineGame = games.get(gameId);
        if (onlineGame == null || onlineGame.isFinished()) {
            return;
        }
        Game game = onlineGame.getGame();
        if (!game.isGameOver()) {
            if (isValidMove(onlineGame, userId)) {
                game.makeMove(row, col);
            }
            if (game.isGameOver()) {
                handleGameOver(onlineGame, game);
            }
        }
    }

    public boolean leaveGame(long gameId, String userId) {
        OnlineGame onlineGame = games.get(gameId);
        if (onlineGame != null) {
            if (userId.equals(onlineGame.getCreatorId())) {
                games.remove(gameId);
                stopTimerForGame(gameId);
                return true;
            }

            boolean playerLeft = removePlayer(onlineGame, userId);
            if (playerLeft) {
                resetGame(onlineGame);
                onlineGame.setWaitingForSecondPlayer(true);
                return false;
            }
        }
        return false;
    }

    public void deleteGame(long gameId) {
        games.remove(gameId);
        stopTimerForGame(gameId);
    }

    public OnlineGame rematchGame(long gameId) {
        OnlineGame onlineGame = games.get(gameId);
        if (onlineGame == null || !onlineGame.isFinished()) {
            return onlineGame;
        }
        resetGameWithoutScores(onlineGame);
        switchPlayers(onlineGame);
        onlineGame.setWaitingForSecondPlayer(
                onlineGame.getPlayerXId() == null || onlineGame.getPlayerOId() == null
        );
        return onlineGame;
    }

    public void stopTimerForGame(long gameId) {
        TimerHandles handles = gameTimers.remove(gameId);
        if (handles != null) {
            handles.cancelAll(true);
        }
    }

    public void startInactivityTimer(long gameId, SimpMessagingTemplate messagingTemplate, int minutes) {

        OnlineGame onlineGame = getOnlineGame(gameId);
        if (onlineGame == null || onlineGame.isFinished()) {
            return;
        }

        stopTimerForGame(gameId);

        long totalSeconds = minutes * 60L;
        long warningSeconds = Math.max(totalSeconds - 30, 0);

        ScheduledFuture<?> warningFuture = scheduler.schedule(() -> {
            OnlineGame g = getOnlineGame(gameId);
            if (g != null) {
                messagingTemplate.convertAndSend(
                        Route.TOPIC_ONLINE_GAME_PREFIX + gameId,
                        WebSocketCommand.TIME_LEFT_30
                );
            }
        }, warningSeconds, TimeUnit.SECONDS);

        ScheduledFuture<?> closeFuture = scheduler.schedule(() -> {
            OnlineGame g = getOnlineGame(gameId);
            if (g != null) {
                deleteGame(gameId);
                messagingTemplate.convertAndSend(
                        Route.TOPIC_ONLINE_GAME_PREFIX + gameId,
                        WebSocketCommand.CLOSED
                );
                messagingTemplate.convertAndSend(Route.TOPIC_GAME_LIST, listGames());
            }
        }, totalSeconds, TimeUnit.SECONDS);

        gameTimers.put(gameId, new TimerHandles(warningFuture, closeFuture));
    }

    private void assignPlayer(OnlineGame onlineGame, String userId, String displayName) {
        if (onlineGame.getPlayerXId() == null) {
            onlineGame.setPlayerXId(userId);
            onlineGame.setPlayerXDisplay(displayName);
        } else if (onlineGame.getPlayerOId() == null) {
            onlineGame.setPlayerOId(userId);
            onlineGame.setPlayerODisplay(displayName);
        }
    }

    private boolean isValidMove(OnlineGame onlineGame, String userId) {
        String currentPlayer = onlineGame.getGame().getCurrentPlayer();
        return (Sign.CROSS.getSign().equals(currentPlayer) && userId.equals(onlineGame.getPlayerXId()))
                || (Sign.NOUGHT.getSign().equals(currentPlayer) && userId.equals(onlineGame.getPlayerOId()));
    }

    private void handleGameOver(OnlineGame onlineGame, Game game) {
        onlineGame.setFinished(true);
        String winner = game.getWinner();

        if (Sign.CROSS.getSign().equals(winner)) {
            onlineGame.setWinnerDisplay(onlineGame.getPlayerXDisplay());
            onlineGame.setScoreX(onlineGame.getScoreX() + 1);
        } else if (Sign.NOUGHT.getSign().equals(winner)) {
            onlineGame.setWinnerDisplay(onlineGame.getPlayerODisplay());
            onlineGame.setScoreO(onlineGame.getScoreO() + 1);
        } else {
            onlineGame.setWinnerDisplay(Key.DRAW);
        }
    }

    private boolean removePlayer(OnlineGame onlineGame, String userId) {
        if (userId.equals(onlineGame.getPlayerXId())) {
            onlineGame.setPlayerXId(null);
            onlineGame.setPlayerXDisplay(null);
            return true;
        } else if (userId.equals(onlineGame.getPlayerOId())) {
            onlineGame.setPlayerOId(null);
            onlineGame.setPlayerODisplay(null);
            return true;
        }
        return false;
    }

    private void resetGame(OnlineGame onlineGame) {
        onlineGame.getGame().resetBoard();
        onlineGame.setScoreX(0);
        onlineGame.setScoreO(0);
        onlineGame.setFinished(false);
        onlineGame.setWinnerDisplay(null);
    }

    private void resetGameWithoutScores(OnlineGame onlineGame) {
        onlineGame.getGame().resetBoard();
        onlineGame.setFinished(false);
        onlineGame.setWinnerDisplay(null);
    }

    private void switchPlayers(OnlineGame onlineGame) {
        String tempId = onlineGame.getPlayerXId();
        String tempDisplay = onlineGame.getPlayerXDisplay();

        onlineGame.setPlayerXId(onlineGame.getPlayerOId());
        onlineGame.setPlayerXDisplay(onlineGame.getPlayerODisplay());
        onlineGame.setPlayerOId(tempId);
        onlineGame.setPlayerODisplay(tempDisplay);

        int tempScore = onlineGame.getScoreX();
        onlineGame.setScoreX(onlineGame.getScoreO());
        onlineGame.setScoreO(tempScore);
    }
}