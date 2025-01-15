package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.RequestParams;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.View;
import com.shubchynskyi.tictactoeapp.domain.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GameController {

    @GetMapping(Route.START_GAME)
    public String startGame(
            @RequestParam(RequestParams.GAME_MODE) String gameMode,
            @RequestParam(value = RequestParams.PLAYER_SYMBOL, required = false) String playerSymbol,
            @RequestParam(value = RequestParams.DIFFICULTY, required = false) String difficulty,
            HttpSession session,
            Model model) {

        session.setAttribute(SessionAttributes.LAST_SYMBOL, playerSymbol);
        session.setAttribute(SessionAttributes.LAST_DIFF, difficulty);

        Game game = new Game(gameMode, playerSymbol, difficulty);
        session.setAttribute(SessionAttributes.LOCAL_GAME, game);

        model.addAttribute(SessionAttributes.LOCAL_GAME, game);
        return View.GAME;
    }
}