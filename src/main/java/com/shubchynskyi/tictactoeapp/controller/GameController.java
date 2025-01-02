package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.model.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Создание новой локальной игры по "/start",
 * затем кладём объект Game в HttpSession (назовём "localGame"),
 * и рендерим game.html
 */
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
        // Создаём Game
        Game game = new Game(gameMode, playerSymbol, difficulty);
        // Храним в сессии
        session.setAttribute("localGame", game);

        // Передаём в модель (можно, чтобы game.html отобразил)
        model.addAttribute("game", game);
        return "game"; // game.html
    }
}
