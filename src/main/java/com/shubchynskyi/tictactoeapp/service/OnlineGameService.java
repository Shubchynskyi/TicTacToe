package com.shubchynskyi.tictactoeapp.service;

import com.shubchynskyi.tictactoeapp.entity.Difficulty;
import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;
import com.shubchynskyi.tictactoeapp.model.OnlineGame;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OnlineGameService {

    private final ConcurrentHashMap<Long, OnlineGame> games = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1000);

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
            if (!og.getPlayerX().equals(nick)) {
                og.setPlayerO(nick);
                og.setWaitingForSecondPlayer(false);
            } else {
                return null; // same nick as X
            }
        }
        return og;
    }

    public OnlineGame makeMove(long gameId, String nick, int row, int col) {
        OnlineGame og = games.get(gameId);
        if (og==null || og.isFinished()) return null;
        Game g = og.getGame();

        if (!g.isGameOver()) {
            String cur = g.getCurrentPlayer(); // "X" or "O"
            if ("X".equals(cur) && nick.equals(og.getPlayerX())) {
                g.makeMove(row, col);
            } else if ("O".equals(cur) && nick.equals(og.getPlayerO())) {
                g.makeMove(row, col);
            }
            if (g.isGameOver()) {
                og.setFinished(true);
                if ("DRAW".equals(g.getWinner())) {
                    og.setWinnerNick("DRAW");
                } else if ("X".equals(g.getWinner())) {
                    og.setWinnerNick(og.getPlayerX());
                } else if ("O".equals(g.getWinner())) {
                    og.setWinnerNick(og.getPlayerO());
                }
            }
        }
        return og;
    }

    public void leaveGame(long gameId, String nick) {
        OnlineGame og = games.get(gameId);
        if (og!=null) {
            if (nick.equals(og.getPlayerX())) {
                og.setPlayerX(null);
            } else if (nick.equals(og.getPlayerO())) {
                og.setPlayerO(null);
            }
            if (og.getPlayerX()==null && og.getPlayerO()==null) {
                games.remove(gameId);
            }
        }
    }

    public void finishGame(long gameId) {
        OnlineGame og = games.get(gameId);
        if (og!=null) {
            og.setFinished(true);
        }
    }
}