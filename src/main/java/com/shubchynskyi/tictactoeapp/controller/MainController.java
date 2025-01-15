package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.constants.View;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;
import java.util.function.Supplier;

@Controller
public class MainController {

    @Value("${app.defaultNick}")
    private String defaultNick;

    @GetMapping(Route.INDEX)
    public String index(HttpSession session, Model model) {
        initializeSessionAttributes(session);
        addModelAttributes(model, session);
        return View.INDEX;
    }

    private void initializeSessionAttributes(HttpSession session) {
        setIfAbsent(session, SessionAttributes.USER_ID, () -> UUID.randomUUID().toString());
        setIfAbsent(session, SessionAttributes.NICK, () -> defaultNick + (System.currentTimeMillis() % 1000));
        setIfAbsent(session, SessionAttributes.LAST_SYMBOL, Sign.CROSS::getSign);
        setIfAbsent(session, SessionAttributes.LAST_DIFF, Difficulty.EASY::getValue);
    }

    private void addModelAttributes(Model model, HttpSession session) {
        model.addAttribute(SessionAttributes.DIFFICULTIES, Difficulty.values());
        model.addAttribute(SessionAttributes.LAST_SYMBOL, session.getAttribute(SessionAttributes.LAST_SYMBOL));
        model.addAttribute(SessionAttributes.LAST_DIFF, session.getAttribute(SessionAttributes.LAST_DIFF));
    }

    private void setIfAbsent(HttpSession session, String key, Supplier<Object> supplier) {
        if (session.getAttribute(key) == null) {
            session.setAttribute(key, supplier.get());
        }
    }
}