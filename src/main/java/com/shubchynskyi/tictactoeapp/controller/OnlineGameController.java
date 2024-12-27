package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.model.OnlineGame;
import com.shubchynskyi.tictactoeapp.service.OnlineGameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    @GetMapping("/online")
    public String showOnlineGames(Model model, HttpSession session) {
        List<OnlineGame> games = service.listGames();
        model.addAttribute("games", games);
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }
        model.addAttribute("nick", nick);
        return "online";
    }

    @PostMapping("/create-online")
    public String createOnline(HttpSession session) {
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }
        long gameId = service.createGame(nick);
        // we can broadcast new game list
        broadcastGameList();
        return "redirect:/online";
    }

    @GetMapping("/join-online")
    public String joinGame(@RequestParam("gameId") long gameId, HttpSession session) {
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }
        service.joinGame(gameId, nick);
        // broadcast updated list
        broadcastGameList();
        return "redirect:/onlineGame?gameId=" + gameId;
    }

    @GetMapping("/onlineGame")
    public String onlineGamePage(@RequestParam("gameId") long gameId, Model model, HttpSession session) {
        OnlineGame og = service.getOnlineGame(gameId);
        if (og == null) {
            return "redirect:/online";
        }
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }
        model.addAttribute("onlineGame", og);
        model.addAttribute("nick", nick);
        return "onlineGame";
    }

    // ============ WebSocket Endpoints ============

    @MessageMapping("/online-move")
    public void handleOnlineMove(OnlineGameMessage msg) {
        service.makeMove(msg.getGameId(), msg.getNick(), msg.getRow(), msg.getCol());
        OnlineGame og = service.getOnlineGame(msg.getGameId());
        messagingTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);
    }

    // no "leave-game" here, let's do it in this same controller or we can do it here:

    @MessageMapping("/leave-game")
    public void handleLeaveGame(LeaveGameMessage msg) {
        service.leaveGame(msg.getGameId(), msg.getNick());
        broadcastGameList();
        // also notify other players that game changed
        OnlineGame og = service.getOnlineGame(msg.getGameId());
        if (og != null) {
            messagingTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);
        }
    }

    // or we can do it in the same class
    public void broadcastGameList() {
        List<OnlineGame> allGames = service.listGames();
        messagingTemplate.convertAndSend("/topic/game-list", allGames);
    }
}