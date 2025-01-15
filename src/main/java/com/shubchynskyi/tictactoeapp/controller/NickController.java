package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NickController {

    @GetMapping(Route.UPDATE_NICK)
    public void updateNick(@RequestParam(SessionAttributes.NICK) String newNick,
                           HttpSession session) {
        if (newNick != null && !newNick.isBlank()) {
            session.setAttribute(SessionAttributes.NICK, newNick);
        }
    }
}