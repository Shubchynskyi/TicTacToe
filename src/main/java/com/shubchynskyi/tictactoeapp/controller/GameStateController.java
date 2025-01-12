package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.domain.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GameStateController {

    @GetMapping(Route.GAME_STATE)
    public Game getLocalGameState(HttpSession session) {
        return retrieveLocalGameFromSession(session);
    }

    private Game retrieveLocalGameFromSession(HttpSession session) {
        Game game = (Game) session.getAttribute(Key.LOCAL_GAME);
        if (game == null) {
            return createDefaultGame();
        }
        return game;
    }

    private Game createDefaultGame() {
        return new Game();
    }
}