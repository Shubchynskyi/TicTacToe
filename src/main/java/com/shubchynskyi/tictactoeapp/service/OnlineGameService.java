package com.shubchynskyi.tictactoeapp.service;

import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;
import com.shubchynskyi.tictactoeapp.model.OnlineGame;
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

    private final Map<Long, ScheduledFuture<?>> gameTimers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public long createGame(String userId, String displayName) {
        long gid = idGen.incrementAndGet();
        OnlineGame og = new OnlineGame(gid, userId, displayName);
        games.put(gid, og);
        return gid;
    }

    public OnlineGame getOnlineGame(long gameId) {
        return games.get(gameId);
    }

    public List<OnlineGame> listGames() {
        return new ArrayList<>(games.values());
    }

    public OnlineGame joinGame(long gameId, String userId, String displayName) {
        OnlineGame og = games.get(gameId);
        if (og != null && og.isWaitingForSecondPlayer()) {
            if (og.getPlayerXId() == null) {
                og.setPlayerXId(userId);
                og.setPlayerXDisplay(displayName);
            } else if (og.getPlayerOId() == null) {
                og.setPlayerOId(userId);
                og.setPlayerODisplay(displayName);
            }
            og.setWaitingForSecondPlayer(false);
        }
        return og;
    }

    public OnlineGame makeMove(long gameId, String userId, int row, int col) {
        OnlineGame og = games.get(gameId);
        if (og == null || og.isFinished()) {
            return null;
        }
        Game g = og.getGame();
        if (!g.isGameOver()) {
            // 1. Проверяем, кто ходит
            String cur = g.getCurrentPlayer(); // "X" или "O"

            // 2. Сопоставляем userId => XId/OId
            if ("X".equals(cur) && userId.equals(og.getPlayerXId())) {
                g.makeMove(row, col);
            } else if ("O".equals(cur) && userId.equals(og.getPlayerOId())) {
                g.makeMove(row, col);
            }

            // 3. Если теперь игра окончена => смотрим победителя
            if (g.isGameOver()) {
                og.setFinished(true);

                // (A) Проверяем winner
                String w = g.getWinner(); // "X", "O" или "DRAW"
                if ("DRAW".equals(w)) {
                    og.setWinnerDisplay("DRAW");
                } else if ("X".equals(w)) {
                    og.setWinnerDisplay(og.getPlayerXDisplay());
                    og.setScoreX(og.getScoreX() + 1);
                } else if ("O".equals(w)) {
                    og.setWinnerDisplay(og.getPlayerODisplay());
                    og.setScoreO(og.getScoreO() + 1);
                }

                // (B) Найдём выигрышную комбинацию (три клетки)
                //     если "DRAW", combo=null
                if (!"DRAW".equals(w)) {
                    // у вас в Game есть checkWinCombo()
                    int[] combo = checkWinCombo(g);
                    og.getGame().setWinningCombo(combo);
                } else {
                    og.getGame().setWinningCombo(null);
                }
            }
        }
        return og;
    }

    // Дополнительный метод в сервисе — найти 3 клетки, используя вашу Game:
    private int[] checkWinCombo(Game g) {
        // используем тот же массив combos:
        int[][] combos = {
                {0,1,2},{3,4,5},{6,7,8},
                {0,3,6},{1,4,7},{2,5,8},
                {0,4,8},{2,4,6}
        };
        for (int[] c : combos) {
            if (g.getSignAt(c[0]) != Sign.EMPTY
                    && g.getSignAt(c[0]) == g.getSignAt(c[1])
                    && g.getSignAt(c[1]) == g.getSignAt(c[2])) {
                return c; // первая найденная тройка
            }
        }
        return null; // нет 3 в ряд
    }

    public boolean leaveGame(long gameId, String userId) {
        OnlineGame og = games.get(gameId);

        if (og != null) {

            // Если уходит создатель => удаляем игру
            if (og.getCreatorId() != null && og.getCreatorId().equals(userId)) {
                games.remove(gameId);  // всё, игры нет
                return true;           // closed
            }

            // Уходит X или O?
            boolean wasX = (og.getPlayerXId() != null && og.getPlayerXId().equals(userId));
            boolean wasO = (og.getPlayerOId() != null && og.getPlayerOId().equals(userId));
            if (wasX) {
                og.setPlayerXId(null);
                og.setPlayerXDisplay(null);
            } else if (wasO) {
                og.setPlayerOId(null);
                og.setPlayerODisplay(null);
            }

            // Если теперь 0 игроков => удаляем
            if (og.getPlayerXId() == null && og.getPlayerOId() == null) {
                games.remove(gameId);
                return true; // closed
            }

            // Иначе остался 1 игрок
            // 1) сбрасываем поле
            og.getGame().resetBoard();
            // 2) сбрасываем счёт
            og.setScoreX(0);
            og.setScoreO(0);
            // 3) finished=false, winnerDisplay=null
            og.setFinished(false);
            og.setWinnerDisplay(null);
            og.getGame().setWinningCombo(null);
            // 4) waitingForSecondPlayer=true
            og.setWaitingForSecondPlayer(true);

            // Возвращаем false => не закрыли игру
            return false;
        }
        return false;
    }

    public void deleteGame(long gameId) {
        games.remove(gameId);
    }

    public OnlineGame rematchGame(long gameId) {
        OnlineGame og = games.get(gameId);
        if (og == null) return null;
        if (!og.isFinished()) return og;

        og.setFinished(false);
        og.setWinnerDisplay(null);
        og.getGame().setWinningCombo(null);

        Game newG;
        boolean rX = (Math.random() < 0.5);
        if (rX) {
            newG = new Game("online", "X", "easy");
        } else {
            newG = new Game("online", "O", "easy");
            String oldXId = og.getPlayerXId();
            String oldXDisp = og.getPlayerXDisplay();
            og.setPlayerXId(og.getPlayerOId());
            og.setPlayerXDisplay(og.getPlayerODisplay());
            og.setPlayerOId(oldXId);
            og.setPlayerODisplay(oldXDisp);

            int tmpScore = og.getScoreX();
            og.setScoreX(og.getScoreO());
            og.setScoreO(tmpScore);
        }
        og.setGame(newG);
        og.setWaitingForSecondPlayer(og.getPlayerXId() == null || og.getPlayerOId() == null);
        return og;
    }

    public void stopTimerForGame(long gameId) {
        ScheduledFuture<?> future = gameTimers.remove(gameId);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void startInactivityTimer(long gameId,
                                     SimpMessagingTemplate msgTemplate,
                                     int minutes) {
        // (1) ОСТАНОВКА СТАРОГО ТАЙМЕРА, если есть
        stopTimerForGame(gameId);

        // (2) Запускаем новый async
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                // Спим (minutes*60 - 30) => TIMELEFT_30
                long half = (minutes * 60L - 30) * 1000L;
                if (half < 0) half = 0;
                Thread.sleep(half);

                OnlineGame og = getOnlineGame(gameId);
                if (og != null && !og.isFinished()) {
                    msgTemplate.convertAndSend(
                            "/topic/online-game-" + gameId, "\"TIMELEFT_30\"");
                }

                // Спим ещё 30 сек => CLOSED
                Thread.sleep(30_000L);
                og = getOnlineGame(gameId);
                if (og != null && !og.isFinished()) {
                    og.setFinished(true);
                    deleteGame(gameId);
                    msgTemplate.convertAndSend(
                            "/topic/online-game-" + gameId, "\"CLOSED\"");
                    // ОБНОВИМ список
                    msgTemplate.convertAndSend("/topic/game-list", listGames());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, TimeUnit.MILLISECONDS);

        // (3) Сохраняем future, чтобы потом отменить
        gameTimers.put(gameId, future);
    }

}