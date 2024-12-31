package com.shubchynskyi.tictactoeapp.service;

import com.shubchynskyi.tictactoeapp.entity.Difficulty;
import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;
import com.shubchynskyi.tictactoeapp.model.OnlineGame;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        if (og==null || og.isFinished()) return null;

        Game g = og.getGame();
        if (!g.isGameOver()) {
            String cur = g.getCurrentPlayer();
            // "X" => og.getPlayerX(), "O" => og.getPlayerO()
            if ("X".equals(cur) && og.getPlayerX()!=null && og.getPlayerX().equals(nick)) {
                g.makeMove(row, col);
            } else if ("O".equals(cur) && og.getPlayerO()!=null && og.getPlayerO().equals(nick)) {
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
                    og.setScoreX(og.getScoreX()+1);
                } else if ("O".equals(w)) {
                    og.setWinnerNick(og.getPlayerO());
                    // <--- ADD score for O
                    og.setScoreO(og.getScoreO()+1);
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
            boolean wasPlayerX = (og.getPlayerX()!=null && og.getPlayerX().equals(nick));
            boolean wasPlayerO = (og.getPlayerO()!=null && og.getPlayerO().equals(nick));

            if (wasPlayerX) og.setPlayerX(null);
            else if (wasPlayerO) og.setPlayerO(null);

            if (og.getPlayerX()==null && og.getPlayerO()==null) {
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

//    public void leaveGame(long gameId, String nick) {
//        OnlineGame og = games.get(gameId);
//        if (og != null) {
//            // <--- 2.1. Если создатель уходит, закрываем комнату
//            if (og.getCreatorNick()!=null && og.getCreatorNick().equals(nick)) {
//                // Удаляем игру
//                games.remove(gameId);
//                return;
//            }
//
//            // <--- 2.2. Если уходит второй игрок - надо обнулить счёт,
//            // освободить слот O (или X), чтоб можно было снова присоединиться
//            boolean wasPlayerX = (og.getPlayerX()!=null && og.getPlayerX().equals(nick));
//            boolean wasPlayerO = (og.getPlayerO()!=null && og.getPlayerO().equals(nick));
//
//            if (wasPlayerX) {
//                og.setPlayerX(null);
//            } else if (wasPlayerO) {
//                og.setPlayerO(null);
//            }
//
//            // Если ушёл «второй», а первый (создатель) остаётся => сбросить счёт
//            // finished=false, waitingForSecondPlayer=true
//            if ((wasPlayerX || wasPlayerO)
//                    && (og.getPlayerX()!=null || og.getPlayerO()!=null) ) {
//                // room not empty => one is still inside
//                og.setScoreX(0);
//                og.setScoreO(0);
//                og.setFinished(false);
//                og.setWinnerNick(null);
//                og.setWaitingForSecondPlayer(true);
//            }
//
//            // Если после выхода в комнате никого не осталось
//            if (og.getPlayerX()==null && og.getPlayerO()==null) {
//                games.remove(gameId);
//            }
//        }
//    }

    public void finishGame(long gameId) {
        OnlineGame og = games.get(gameId);
        if (og!=null) {
            og.setFinished(true);
        }
    }

    // <--- ADD: rematchGame, меняем X/O заново
    public OnlineGame rematchGame(long gameId) {
        OnlineGame og = games.get(gameId);
        if (og==null) return null;
        if (!og.isFinished()) return og; // игра не закончена

        // очищаем поле, winnerNick, finished
        og.setFinished(false);
        og.setWinnerNick(null);
        Game g;

        // Счёт уже увеличен при makeMove

        boolean randomX = (Math.random()<0.5);
        if (randomX) {
            g = new Game("online","X","easy");
        } else {
            g = new Game("online","O","easy");
            String playerX = og.getPlayerX();
            og.setPlayerX(og.getPlayerO());
            og.setPlayerO(playerX);
            int scoreX = og.getScoreX();
            og.setScoreX(og.getScoreO());
            og.setScoreO(scoreX);
        }
        og.setWaitingForSecondPlayer( (og.getPlayerX()==null || og.getPlayerO()==null) );
        og.setFinished(false);
        og.setWinnerNick(null);

        og.setGame(g);
        return og;
    }
}