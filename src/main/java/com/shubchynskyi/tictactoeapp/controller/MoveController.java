package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.model.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MoveController {

    @GetMapping("/make-move")
    public Game makeLocalMove(
            @RequestParam("row") int row,
            @RequestParam("col") int col,
            HttpSession session
    ) {
        Game game = (Game) session.getAttribute("localGame");
        if (game == null) {
            game = new Game("single", "X", null);
            session.setAttribute("localGame", game);
        }
        game.makeMove(row, col);
        game.makeAiMoveIfNeeded();
        return game;
    }
}

