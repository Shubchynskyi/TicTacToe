package com.shubchynskyi.tictactoeapp.service;

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
    private final AtomicLong idGenerator = new AtomicLong(1000);

    public long createGame(String creatorNick) {
        long id = idGenerator.incrementAndGet();
        OnlineGame og = new OnlineGame(id, creatorNick);
        games.put(id, og);
        return id;
    }

    public OnlineGame joinGame(long gameId, String joinerNick) {
        OnlineGame og = games.get(gameId);
        if (og != null && og.isWaitingForSecondPlayer()) {
            og.setPlayerO(joinerNick);
            og.setWaitingForSecondPlayer(false);
        }
        return og;
    }

    public List<OnlineGame> listGames() {
        return new ArrayList<>(games.values());
    }

    public OnlineGame getOnlineGame(long gameId) {
        return games.get(gameId);
    }

    public Game makeMove(long gameId, String nick, int row, int col) {
        OnlineGame og = games.get(gameId);
        if (og != null) {
            Game g = og.getGame();
            if (!g.isGameOver()) {
                if (g.getCurrentPlayer().equals("X") && nick.equals(og.getPlayerX())) {
                    g.makeMove(row, col);
                } else if (g.getCurrentPlayer().equals("O") && nick.equals(og.getPlayerO())) {
                    g.makeMove(row, col);
                }
            }
            return g;
        }
        return null;
    }
}
