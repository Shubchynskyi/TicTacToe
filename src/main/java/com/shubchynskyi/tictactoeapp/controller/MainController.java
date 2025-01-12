package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class MainController {

    @Value("${app.defaultNick}")
    private String defaultNick;

    @GetMapping(Route.INDEX)
    public String index(HttpSession session, Model model) {
        initializeSessionAttributes(session);
        addModelAttributes(model, session);
        return Key.GAME_VIEW;
    }

    private void initializeSessionAttributes(HttpSession session) {
        setUserIdIfNotExists(session);
        setNickIfNotExists(session);
        setLastSymbolIfNotExists(session);
        setLastDifficultyIfNotExists(session);
    }

    private void setUserIdIfNotExists(HttpSession session) {
        if (session.getAttribute(Key.USER_ID) == null) {
            String userId = UUID.randomUUID().toString();
            session.setAttribute(Key.USER_ID, userId);
        }
    }

    private void setNickIfNotExists(HttpSession session) {
        if (session.getAttribute(Key.NICK) == null) {
            long rnd = System.currentTimeMillis() % 1000;
            session.setAttribute(Key.NICK, defaultNick + rnd);
        }
    }

    private void setLastSymbolIfNotExists(HttpSession session) {
        if (session.getAttribute(Key.LAST_SYMBOL) == null) {
            session.setAttribute(Key.LAST_SYMBOL, Character.toString(Sign.CROSS.getSign()));
        }
    }

    private void setLastDifficultyIfNotExists(HttpSession session) {
        if (session.getAttribute(Key.LAST_DIFF) == null) {
            session.setAttribute(Key.LAST_DIFF, Difficulty.EASY.getValue());
        }
    }

    private void addModelAttributes(Model model, HttpSession session) {
        model.addAttribute(Key.LAST_SYMBOL, session.getAttribute(Key.LAST_SYMBOL));
        model.addAttribute(Key.LAST_DIFF, session.getAttribute(Key.LAST_DIFF));
        model.addAttribute(Key.DIFFICULTIES, Difficulty.values());
    }
}