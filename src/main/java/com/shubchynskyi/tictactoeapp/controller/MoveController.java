package com.shubchynskyi.tictactoeapp.controller;


import com.shubchynskyi.tictactoeapp.model.Game;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ход локальной игры: GET "/make-move?row=..&col=.."
 * Возвращает обновлённое Game (JSON)
 */
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
            // создаём новую по умолчанию, single
            game = new Game("single","X","easy");
            session.setAttribute("localGame", game);
        }
        game.makeMove(row, col);
        return game;
    }

    @GetMapping("/restart-local")
    public Game restartLocal(HttpSession session) {
        Game g = (Game) session.getAttribute("localGame");
        if (g != null) {
            // сбрасываем board, winner, currentPlayer
            g.resetBoard(); // новый метод
        }
        return g;
    }

}

