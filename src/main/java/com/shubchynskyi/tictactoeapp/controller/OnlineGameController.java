package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.model.OnlineGame;
import com.shubchynskyi.tictactoeapp.service.OnlineGameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Comments in English: We'll have some HTTP endpoints for listing/joining, but moves will be handled via WebSocket.
@Controller
public class OnlineGameController {

    private final OnlineGameService service;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public OnlineGameController(OnlineGameService service,
                                SimpMessagingTemplate messagingTemplate) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
    }

    // Show the page with the list of online games
    @GetMapping("/online")
    public String showOnlineGames(Model model, HttpSession session) {
        List<OnlineGame> games = service.listGames();
        model.addAttribute("games", games);
        String nick = (String) session.getAttribute("nick");
        model.addAttribute("nick", nick);
        return "online";
    }

    // Create new game
    @PostMapping("/create-online")
    public String createOnline(HttpSession session) {
        String nick = (String) session.getAttribute("nick");
        if(nick == null) {
            nick = "Guest" + (System.currentTimeMillis()%1000);
            session.setAttribute("nick", nick);
        }
        long gameId = service.createGame(nick);
        return "redirect:/online";
    }

    // Join game
    @GetMapping("/join-online")
    public String joinGame(@RequestParam("gameId") long gameId, HttpSession session) {
        String nick = (String) session.getAttribute("nick");
        if(nick == null) {
            nick = "Guest" + (System.currentTimeMillis()%1000);
            session.setAttribute("nick", nick);
        }
        service.joinGame(gameId, nick);
        return "redirect:/onlineGame?gameId=" + gameId;
    }

    // The actual game page
    @GetMapping("/onlineGame")
    public String onlineGamePage(@RequestParam("gameId") long gameId, Model model, HttpSession session) {
        OnlineGame og = service.getOnlineGame(gameId);
        if(og == null) {
            return "redirect:/online";
        }
        model.addAttribute("onlineGame", og);
        String nick = (String) session.getAttribute("nick");
        model.addAttribute("nick", nick);
        return "onlineGame";
    }

    // ====================== WebSocket endpoints =========================
    // The client sends a message to /app/online-move
    // We broadcast updated state to /topic/online-game-{gameId}

    @MessageMapping("/online-move")
    public void handleOnlineMove(OnlineGameMessage msg) {
        service.makeMove(msg.getGameId(), msg.getNick(), msg.getRow(), msg.getCol());
        // Now we get the updated OnlineGame
        OnlineGame og = service.getOnlineGame(msg.getGameId());
        // We manually send to the topic that corresponds to this game
        messagingTemplate.convertAndSend(
                "/topic/online-game-" + msg.getGameId(),
                og
        );
    }


}
