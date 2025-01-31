package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.domain.Game;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class GameStateController {

    @GetMapping(Route.GAME_STATE)
    public Game getLocalGameState(HttpSession session) {
        log.info("Fetching local game state from session...");
        return retrieveLocalGame(session);
    }

    private Game retrieveLocalGame(HttpSession session) {
        Game game = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);

        if (game != null) {
            log.info("Retrieved existing game state: {}", game);
            return game;
        } else {
            log.warn("No game found in session, creating default game.");
            return createDefaultGame();
        }
    }

    private Game createDefaultGame() {
        Game defaultGame = new Game();
        log.info("Created new default game: {}", defaultGame);
        return defaultGame;
    }
}