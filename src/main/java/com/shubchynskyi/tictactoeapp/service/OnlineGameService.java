package com.shubchynskyi.tictactoeapp.service;

import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.domain.OnlineGame;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import jakarta.annotation.PostConstruct;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OnlineGameService {

    private final ConcurrentHashMap<Long, OnlineGame> games = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1000);

    // Вместо одного ScheduledFuture храним "пару" задач для каждой игры
    private final Map<Long, TimerHandles> gameTimers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // ---------------------------
    //  Вспомогательный класс
    // ---------------------------
    private static class TimerHandles {
        private final ScheduledFuture<?> warningFuture;
        private final ScheduledFuture<?> closeFuture;

        public TimerHandles(ScheduledFuture<?> warningFuture, ScheduledFuture<?> closeFuture) {
            this.warningFuture = warningFuture;
            this.closeFuture = closeFuture;
        }

        public void cancelAll(boolean mayInterruptIfRunning) {
            if (warningFuture != null) {
                warningFuture.cancel(mayInterruptIfRunning);
            }
            if (closeFuture != null) {
                closeFuture.cancel(mayInterruptIfRunning);
            }
        }
    }

    // -----------------------------------------------------------
    //  Пример: метод для периодического логирования
    // -----------------------------------------------------------
    public void startPeriodicLogging(int periodInSeconds) {
        Runnable logTask = () -> {
            try {
                List<OnlineGame> currentGames = listGames();
                System.err.println("=== Current online games (" + currentGames.size() + ") ===");
                for (OnlineGame g : currentGames) {
                    System.err.println(g);
                }
                System.err.println("==============================================");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // Запускаем задачу в отдельном потоке с периодом periodInSeconds
        scheduler.scheduleAtFixedRate(logTask, 0, periodInSeconds, TimeUnit.SECONDS);
    }

    @PostConstruct
    public void initLogging() {
        // Допустим, хотим логировать каждые 10 секунд
        startPeriodicLogging(10);
    }

    // -----------------------------------------------------------
    //  Методы CRUD и логика игры
    // -----------------------------------------------------------

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
            // Если вышел создатель, удаляем игру целиком
            if (userId.equals(onlineGame.getCreatorId())) {
                games.remove(gameId);
                stopTimerForGame(gameId); // Останавливаем таймеры
                return true;
            }

            // Если вышел второй игрок, освобождаем его слот
            boolean playerLeft = removePlayer(onlineGame, userId);
            if (playerLeft) {
                // Сбрасываем игру, возвращаем в состояние ожидания
                resetGame(onlineGame);
                onlineGame.setWaitingForSecondPlayer(true);
                // Таймер можно перезапустить, если хотите дать время на повторное подключение
                return false;
            }
        }
        return false;
    }

    public void deleteGame(long gameId) {
        games.remove(gameId);
        stopTimerForGame(gameId); // При удалении игры также останавливаем таймер
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

    // -----------------------------------------------------------
    //  Таймеры
    // -----------------------------------------------------------

    // Метод остановки (отмены) таймеров для игры
    public void stopTimerForGame(long gameId) {
        TimerHandles handles = gameTimers.remove(gameId);
        if (handles != null) {
            handles.cancelAll(true);
        }
    }

    /**
     * Запускаем два таймера:
     * 1. warningFuture — за 30 секунд до конца отправляет "TIMELEFT_30"
     * 2. closeFuture   — по окончании времени закрывает игру
     */
    public void startInactivityTimer(long gameId, SimpMessagingTemplate msgTemplate, int minutes) {
        // Сначала отменяем старые, если были
        stopTimerForGame(gameId);

        long totalSeconds = minutes * 60L;
        long warningSeconds = Math.max(totalSeconds - 30, 0);

        // 1) Запланировать оповещение за 30 секунд до конца
        ScheduledFuture<?> warningFuture = scheduler.schedule(() -> {
            OnlineGame onlineGame = getOnlineGame(gameId);
            if (onlineGame != null && !onlineGame.isFinished()) {
                msgTemplate.convertAndSend("/topic/online-game-" + gameId, "\"TIMELEFT_30\"");
            }
        }, warningSeconds, TimeUnit.SECONDS);

        // 2) Запланировать фактическое закрытие игры
        ScheduledFuture<?> closeFuture = scheduler.schedule(() -> {
            OnlineGame onlineGame = getOnlineGame(gameId);
            if (onlineGame != null && !onlineGame.isFinished()) {
                onlineGame.setFinished(true);
                deleteGame(gameId); // удаляем саму игру и отменяем таймеры
                msgTemplate.convertAndSend("/topic/online-game-" + gameId, "\"CLOSED\"");
                // Не забудьте оповестить список игр:
                msgTemplate.convertAndSend("/topic/game-list", listGames());
            }
        }, totalSeconds, TimeUnit.SECONDS);

        // Сохраняем обе задачи в Map
        gameTimers.put(gameId, new TimerHandles(warningFuture, closeFuture));
    }

    // -----------------------------------------------------------
    //  Вспомогательные методы
    // -----------------------------------------------------------

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
        Game game = onlineGame.getGame();
        String currentPlayer = game.getCurrentPlayer();
        return ("X".equals(currentPlayer) && userId.equals(onlineGame.getPlayerXId()))
                || ("O".equals(currentPlayer) && userId.equals(onlineGame.getPlayerOId()));
    }

    private void handleGameOver(OnlineGame onlineGame, Game game) {
        onlineGame.setFinished(true);
        String winner = game.getWinner();
        onlineGame.setWinnerDisplay(getWinnerDisplay(winner, onlineGame));
        updateScores(onlineGame, winner);
        int[] winningCombo = checkWinCombo(game);
        onlineGame.getGame().setWinningCombo(winningCombo);
    }

    private String getWinnerDisplay(String winner, OnlineGame onlineGame) {
        if ("DRAW".equals(winner)) {
            return "DRAW";
        } else if ("X".equals(winner)) {
            return onlineGame.getPlayerXDisplay();
        } else {
            return onlineGame.getPlayerODisplay();
        }
    }

    private void updateScores(OnlineGame onlineGame, String winner) {
        if ("X".equals(winner)) {
            onlineGame.setScoreX(onlineGame.getScoreX() + 1);
        } else if ("O".equals(winner)) {
            onlineGame.setScoreO(onlineGame.getScoreO() + 1);
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
        onlineGame.getGame().setWinningCombo(null);
    }

    private void resetGameWithoutScores(OnlineGame onlineGame) {
        onlineGame.getGame().resetBoard();
        onlineGame.setFinished(false);
        onlineGame.setWinnerDisplay(null);
        onlineGame.getGame().setWinningCombo(null);
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

    private int[] checkWinCombo(Game game) {
        int[][] combos = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] combo : combos) {
            if (game.getSignAt(combo[0]) != Sign.EMPTY
                    && game.getSignAt(combo[0]) == game.getSignAt(combo[1])
                    && game.getSignAt(combo[1]) == game.getSignAt(combo[2])) {
                return combo;
            }
        }
        return null;
    }
}