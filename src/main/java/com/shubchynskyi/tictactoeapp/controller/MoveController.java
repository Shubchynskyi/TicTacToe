package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.*;
import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MoveController {

    @GetMapping(Route.MAKE_MOVE)
    public Game makeLocalMove(
            @RequestParam(RequestParams.ROW) int row,
            @RequestParam(RequestParams.COL) int col,
            HttpSession session
    ) {
        log.info("Processing move at row: {}, col: {}", row, col);
        Game game = retrieveLocalGame(session);
        game.makeMove(row, col);
        log.info("Move processed successfully. Current game state: {}", game);
        return game;
    }

    @GetMapping(Route.RESTART_LOCAL)
    public Game restartLocal(HttpSession session) {
        log.info("Restarting local game...");
        Game game = retrieveLocalGame(session);
        game.resetBoard();
        log.info("Game restarted. Current game state: {}", game);
        return game;
    }

    private Game retrieveLocalGame(HttpSession session) {
        Game game = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);
        if (game == null) {
            log.warn("No game found in session. Creating a new default game.");
            game = createDefaultGame();
            session.setAttribute(SessionAttributes.LOCAL_GAME, game);
        } else {
            log.info("Retrieved existing game from session.");
        }
        return game;
    }

    private Game createDefaultGame() {
        Game defaultGame = new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.EASY.getValue());
        log.info("Created new default game: {}", defaultGame);
        return defaultGame;
    }
}