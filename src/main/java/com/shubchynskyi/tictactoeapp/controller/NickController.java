package com.shubchynskyi.tictactoeapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NickController {

    @GetMapping("/update-nick")
    public void updateNick(@RequestParam("nick") String newNick,
                           HttpSession session) {
        if (newNick != null && !newNick.isBlank()) {
            session.setAttribute("nick", newNick);
        }
    }
}