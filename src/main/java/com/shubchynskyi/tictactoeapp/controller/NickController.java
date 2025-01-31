package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class NickController {

    @GetMapping(Route.UPDATE_NICK)
    public void updateNick(@RequestParam(SessionAttributes.NICK) String newNick,
                           HttpSession session) {
        log.info("Received request to update nickname: {}", newNick);

        if (newNick != null && !newNick.isBlank()) {
            session.setAttribute(SessionAttributes.NICK, newNick);
            log.info("Nickname successfully updated to: {}", newNick);
        } else {
            log.warn("Invalid nickname provided, update ignored.");
        }
    }
}