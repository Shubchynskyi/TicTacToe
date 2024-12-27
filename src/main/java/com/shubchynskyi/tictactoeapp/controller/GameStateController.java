package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.model.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Возвращает текущее состояние localGame (JSON) при GET "/game-state"
 */
@RestController
public class GameStateController {

    @GetMapping("/game-state")
    public Game getLocalGameState(HttpSession session) {
        Game game = (Game) session.getAttribute("localGame");
        if (game == null) {
            // вернуть пустой Game, чтобы JSON не упал
            return new Game();
        }
        return game;
    }
}