package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.model.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameStateController {

    @GetMapping("/game-state")
    public Game getLocalGameState(HttpSession session) {
        Game game = (Game) session.getAttribute("localGame");
        if (game == null) {
            return new Game();
        }
        return game;
    }
}