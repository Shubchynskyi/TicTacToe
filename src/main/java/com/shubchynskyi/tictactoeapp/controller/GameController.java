package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.domain.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class GameController {

    @GetMapping("/start")
    public String startGame(
            @RequestParam("gameMode") String gameMode,
            @RequestParam(value="playerSymbol", required=false) String playerSymbol,
            @RequestParam(value="difficulty", required=false) String difficulty,
            HttpSession session,
            Model model
    ) {
        session.setAttribute("lastSymbol", playerSymbol);
        session.setAttribute("lastDiff", difficulty);

        Game game = new Game(gameMode, playerSymbol, difficulty);

        session.setAttribute("localGame", game);

        model.addAttribute("game", game);
        return "game";
    }
}