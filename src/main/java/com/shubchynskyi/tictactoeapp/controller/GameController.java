package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.domain.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class GameController {

    @GetMapping(Route.ONLINE)
    public String startGame(
            @RequestParam(Key.GAME_MODE) String gameMode,
            @RequestParam(value = Key.PLAYER_SYMBOL, required = false) String playerSymbol,
            @RequestParam(value = Key.DIFFICULTY, required = false) String difficulty,
            HttpSession session,
            Model model
    ) {
        initializeSessionAttributes(session, playerSymbol, difficulty);
        Game game = createGame(gameMode, playerSymbol, difficulty);
        session.setAttribute(Key.LOCAL_GAME, game);

        model.addAttribute(Key.GAME, game);
        return Key.GAME_VIEW;
    }

    private void initializeSessionAttributes(HttpSession session, String playerSymbol, String difficulty) {
        session.setAttribute(Key.LAST_SYMBOL, playerSymbol);
        session.setAttribute(Key.LAST_DIFF, difficulty);
    }

    private Game createGame(String gameMode, String playerSymbol, String difficulty) {
        return new Game(gameMode, playerSymbol, difficulty);
    }
}