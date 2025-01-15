package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.*;
import com.shubchynskyi.tictactoeapp.domain.Game;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MoveController {

    @GetMapping(Route.MAKE_MOVE)
    public Game makeLocalMove(
            @RequestParam(RequestParams.ROW) int row,
            @RequestParam(RequestParams.COL) int col,
            HttpSession session
    ) {
        Game game = retrieveLocalGame(session);
        game.makeMove(row, col);
        return game;
    }

    @GetMapping(Route.RESTART_LOCAL)
    public Game restartLocal(HttpSession session) {
        Game game = retrieveLocalGame(session);
        game.resetBoard();
        return game;
    }

    private Game retrieveLocalGame(HttpSession session) {
        Game game = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);
        if (game == null) {
            game = createDefaultGame();
            session.setAttribute(SessionAttributes.LOCAL_GAME, game);
        }
        return game;
    }

    private Game createDefaultGame() {
        return new Game(Key.SINGLE_GAME_MOD, Sign.CROSS.getSign(), Difficulty.EASY.getValue());
    }
}