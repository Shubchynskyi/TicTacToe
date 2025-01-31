package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.SessionAttributes;
import com.shubchynskyi.tictactoeapp.constants.View;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Controller
public class MainController {

    @Value("${app.defaultNick}")
    private String defaultNick;

    @GetMapping(Route.INDEX)
    public String index(HttpSession session, Model model) {
        log.info("Accessing index page: clearing session game state");
        session.removeAttribute(SessionAttributes.LOCAL_GAME);

        initializeSessionAttributes(session);
        addModelAttributes(model, session);

        return View.INDEX;
    }

    private void initializeSessionAttributes(HttpSession session) {
        log.info("Initializing session attributes...");
        setIfAbsent(session, SessionAttributes.USER_ID, () -> {
            String userId = UUID.randomUUID().toString();
            log.info("Generated new user ID: {}", userId);
            return userId;
        });

        setIfAbsent(session, SessionAttributes.NICK, () -> {
            String nick = defaultNick + (System.currentTimeMillis() % 1000);
            log.info("Generated new user nickname: {}", nick);
            return nick;
        });

        setIfAbsent(session, SessionAttributes.LAST_SYMBOL, () -> {
            String sign = Sign.CROSS.getSign();
            log.info("Default player sign set to: {}", sign);
            return sign;
        });

        setIfAbsent(session, SessionAttributes.LAST_DIFF, () -> {
            String difficulty = Difficulty.EASY.getValue();
            log.info("Default game difficulty set to: {}", difficulty);
            return difficulty;
        });
    }

    private void addModelAttributes(Model model, HttpSession session) {
        log.info("Adding attributes to model...");
        model.addAttribute(SessionAttributes.DIFFICULTIES, Difficulty.values());
        model.addAttribute(SessionAttributes.LAST_SYMBOL, session.getAttribute(SessionAttributes.LAST_SYMBOL));
        model.addAttribute(SessionAttributes.LAST_DIFF, session.getAttribute(SessionAttributes.LAST_DIFF));
    }

    private void setIfAbsent(HttpSession session, String key, Supplier<Object> supplier) {
        if (session.getAttribute(key) == null) {
            Object value = supplier.get();
            session.setAttribute(key, value);
            log.info("Set session attribute: {} = {}", key, value);
        } else {
            log.debug("Session attribute {} already set: {}", key, session.getAttribute(key));
        }
    }
}