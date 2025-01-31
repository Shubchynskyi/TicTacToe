package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.RequestParams;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.View;
import com.shubchynskyi.tictactoeapp.domain.Game;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class GameController {

    @GetMapping(Route.START_GAME)
    public String startGame(
            @RequestParam(RequestParams.GAME_MODE) String gameMode,
            @RequestParam(value = RequestParams.PLAYER_SYMBOL, required = false) String playerSymbol,
            @RequestParam(value = RequestParams.DIFFICULTY, required = false) String difficulty,
            HttpSession session,
            Model model) {

        log.info("Starting game with mode: {}, playerSymbol: {}, difficulty: {}", gameMode, playerSymbol, difficulty);

        session.setAttribute(SessionAttributes.LAST_SYMBOL, playerSymbol);
        session.setAttribute(SessionAttributes.LAST_DIFF, difficulty);

        Game existing = (Game) session.getAttribute(SessionAttributes.LOCAL_GAME);

        if (existing == null) {
            Game newGame = new Game(gameMode, playerSymbol, difficulty);
            session.setAttribute(SessionAttributes.LOCAL_GAME, newGame);
            model.addAttribute(SessionAttributes.LOCAL_GAME, newGame);
            log.info("Created new game: {}", newGame);
        } else {
            model.addAttribute(SessionAttributes.LOCAL_GAME, existing);
            log.info("Loaded existing game from session: {}", existing);
        }
        return View.GAME;
    }
}