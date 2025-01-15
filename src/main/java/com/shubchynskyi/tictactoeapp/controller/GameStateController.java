package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.domain.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameStateController {

    @GetMapping(Route.GAME_STATE)
    public Game getLocalGameState(HttpSession session) {
        return retrieveLocalGame(session);
    }

    private Game retrieveLocalGame(HttpSession session) {
        Game game = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);
        return (game != null) ? game : createDefaultGame();
    }

    private Game createDefaultGame() {
        return new Game();
    }
}