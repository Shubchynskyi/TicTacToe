package com.shubchynskyi.tictactoeapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        if (session.getAttribute("nick") == null) {
            long rnd = System.currentTimeMillis() % 1000;
            session.setAttribute("nick", "Guest" + rnd);
        }

        String lastSymbol = (String) session.getAttribute("lastSymbol");
        if(lastSymbol==null) lastSymbol="X";
        model.addAttribute("lastSymbol", lastSymbol);

        String lastDiff = (String) session.getAttribute("lastDiff");
        if(lastDiff==null) lastDiff="easy";
        model.addAttribute("lastDiff", lastDiff);

        return "index";
    }
}