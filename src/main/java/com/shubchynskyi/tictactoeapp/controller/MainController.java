package com.shubchynskyi.tictactoeapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("nick") == null) {
            long rnd = System.currentTimeMillis() % 1000;
            session.setAttribute("nick", "Guest" + rnd);
        }
        return "index";
    }
}