package com.shubchynskyi.tictactoeapp.service;

import com.shubchynskyi.tictactoeapp.model.Game;
import com.shubchynskyi.tictactoeapp.model.OnlineGame;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OnlineGameService {

    private final ConcurrentHashMap<Long, OnlineGame> games = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1000);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public long createGame(String creatorNick) {
        long gid = idGen.incrementAndGet();
        OnlineGame og = new OnlineGame(gid, creatorNick);
        games.put(gid, og);
        return gid;
    }

    public OnlineGame getOnlineGame(long gameId) {
        return games.get(gameId);
    }

    public List<OnlineGame> listGames() {
        return new ArrayList<>(games.values());
    }

    public OnlineGame joinGame(long gameId, String nick) {
        OnlineGame og = games.get(gameId);
        if (og != null && og.isWaitingForSecondPlayer()) {

            // <--- CHANGE: проверяем, playerX != null, чтоб избежать NPE
            if (og.getPlayerX() != null && og.getPlayerX().equals(nick)) {
                // тот же ник как X
                return null;
            }
            if (og.getPlayerO() != null && og.getPlayerO().equals(nick)) {
                // тот же ник как O
                return null;
            }

            // Если creatorNick оказался O => playerX=null
            // тогда сетим X = joinerNick
            if (og.getPlayerX() == null) {
                og.setPlayerX(nick);
            } else if (og.getPlayerO() == null) {
                og.setPlayerO(nick);
            }

            og.setWaitingForSecondPlayer(false);
        }
        return og;
    }

    public OnlineGame makeMove(long gameId, String nick, int row, int col) {
        OnlineGame og = games.get(gameId);
        if (og == null || og.isFinished()) return null;

        Game g = og.getGame();
        if (!g.isGameOver()) {
            String cur = g.getCurrentPlayer();
            // "X" => og.getPlayerX(), "O" => og.getPlayerO()
            if ("X".equals(cur) && og.getPlayerX() != null && og.getPlayerX().equals(nick)) {
                g.makeMove(row, col);
            } else if ("O".equals(cur) && og.getPlayerO() != null && og.getPlayerO().equals(nick)) {
                g.makeMove(row, col);
            }

            if (g.isGameOver()) {
                og.setFinished(true);
                String w = g.getWinner();
                if ("DRAW".equals(w)) {
                    og.setWinnerNick("DRAW");
                } else if ("X".equals(w)) {
                    og.setWinnerNick(og.getPlayerX());
                    // <--- ADD score for X
                    og.setScoreX(og.getScoreX() + 1);
                } else if ("O".equals(w)) {
                    og.setWinnerNick(og.getPlayerO());
                    // <--- ADD score for O
                    og.setScoreO(og.getScoreO() + 1);
                }
            }
        }
        return og;
    }

    public boolean leaveGame(long gameId, String nick) {
        OnlineGame og = games.get(gameId);
        if (og != null) {
            if (og.getCreatorNick() != null && og.getCreatorNick().equals(nick)) {
                // creator left => remove game
                games.remove(gameId);
                return true; // room closed
            }
            boolean wasPlayerX = (og.getPlayerX() != null && og.getPlayerX().equals(nick));
            boolean wasPlayerO = (og.getPlayerO() != null && og.getPlayerO().equals(nick));

            if (wasPlayerX) og.setPlayerX(null);
            else if (wasPlayerO) og.setPlayerO(null);

            if (og.getPlayerX() == null && og.getPlayerO() == null) {
                games.remove(gameId);
                return true; // both left => room closed
            }
            // else, second leaves => reset scoreboard, etc.
            og.setScoreX(0);
            og.setScoreO(0);
            og.setFinished(false);
            og.setWinnerNick(null);
            og.setWaitingForSecondPlayer(true);

            return false; // room not closed, but one slot is free
        }
        return false;
    }

    public void finishGame(long gameId) {
        OnlineGame og = games.get(gameId);
        if (og != null) {
            og.setFinished(true);
        }
    }

    // <--- ADD: rematchGame, меняем X/O заново
    public OnlineGame rematchGame(long gameId) {
        OnlineGame og = games.get(gameId);
        if (og == null) return null;
        if (!og.isFinished()) return og; // игра не закончена

        // очищаем поле, winnerNick, finished
        og.setFinished(false);
        og.setWinnerNick(null);
        Game g;

        // Счёт уже увеличен при makeMove

        boolean randomX = (Math.random() < 0.5);
        if (randomX) {
            g = new Game("online", "X", "easy");
        } else {
            g = new Game("online", "O", "easy");
            String playerX = og.getPlayerX();
            og.setPlayerX(og.getPlayerO());
            og.setPlayerO(playerX);
            int scoreX = og.getScoreX();
            og.setScoreX(og.getScoreO());
            og.setScoreO(scoreX);
        }
        og.setWaitingForSecondPlayer((og.getPlayerX() == null || og.getPlayerO() == null));
        og.setFinished(false);
        og.setWinnerNick(null);

        og.setGame(g);
        return og;
    }

    public void deleteGame(long gameId) {
        games.remove(gameId);
    }

    public void startInactivityTimer(long gameId,
                                     SimpMessagingTemplate msgTemplate,
                                     int minutes) {
        CompletableFuture.runAsync(() -> {
            try {
                // Спим (minutes - 0.5) => TIMELEFT_30
                long half = (minutes * 60L - 30) * 1000L;
                if (half < 0) half=0; // edge case
                Thread.sleep(half);

                OnlineGame og = getOnlineGame(gameId);
                if(og!=null && !og.isFinished()) {
                    msgTemplate.convertAndSend(
                            "/topic/online-game-"+gameId, "\"TIMELEFT_30\"");
                }

                // Спим 30с => CLOSED
                Thread.sleep(30_000L);
                og = getOnlineGame(gameId);
                if (og!=null && !og.isFinished()) {
                    og.setFinished(true);
                    deleteGame(gameId);
                    msgTemplate.convertAndSend(
                            "/topic/online-game-"+gameId, "\"CLOSED\""
                    );
                    // ОБНОВИМ список
                    msgTemplate.convertAndSend(
                            "/topic/game-list", listGames()
                    );
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, Executors.newSingleThreadExecutor());
    }


}