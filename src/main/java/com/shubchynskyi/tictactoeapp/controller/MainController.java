package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.entity.Difficulty;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        // проверка userId
        if (session.getAttribute("userId") == null) {
            String userId = UUID.randomUUID().toString();
            session.setAttribute("userId", userId);
        }
        // проверка nick
        if (session.getAttribute("nick") == null) {
            long rnd = System.currentTimeMillis() % 1000;
            session.setAttribute("nick", "Guest" + rnd);
        }

//        String lastSymbol = (String) session.getAttribute("lastSymbol");
//        if (lastSymbol == null) lastSymbol = "X";
//        model.addAttribute("lastSymbol", lastSymbol);
//
//        String lastDiff = (String) session.getAttribute("lastDiff");
//        if (lastDiff == null) lastDiff = "easy";
//        model.addAttribute("lastDiff", lastDiff);
//
//        return "index"; // index.html
        if (session.getAttribute("lastSymbol") == null) {
            session.setAttribute("lastSymbol", "X");
        }
        if (session.getAttribute("lastDiff") == null) {
            session.setAttribute("lastDiff", "easy");
        }
        model.addAttribute("lastSymbol", session.getAttribute("lastSymbol"));
        model.addAttribute("lastDiff", session.getAttribute("lastDiff"));

        // Подгружаем все Difficulty
        model.addAttribute("difficulties", Difficulty.values());

        return "index";
    }
}