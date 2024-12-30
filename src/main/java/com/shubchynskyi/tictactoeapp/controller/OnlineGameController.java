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
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

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
        long gameId = service.createGame(nick); //TODO
        // we can broadcast new game list
        broadcastGameList();
        return "redirect:/onlineGame?gameId=" + gameId;
    }

    @GetMapping("/join-online")
    public String joinGame(@RequestParam("gameId") long gameId, HttpSession session) {
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }
        service.joinGame(gameId, nick);

        // <--- ADD: оповестить комнату, что появился второй игрок
        OnlineGame og = service.getOnlineGame(gameId);
        if (og != null) {
            messagingTemplate.convertAndSend(
                    "/topic/online-game-" + gameId,
                    og
            );
        }
        // <--- /ADD

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

        // <--- ADD: если 2 игрока уже, и nick не равен X и не равен O, редирект
        if (og.getPlayerX() != null && og.getPlayerO() != null) {
            boolean isPlayer = nick.equals(og.getPlayerX()) || nick.equals(og.getPlayerO());
            if (!isPlayer) {
                // не даём зайти
                return "redirect:/online";
            }
        }
        // <--- /ADD

        model.addAttribute("onlineGame", og);
        model.addAttribute("nick", nick);
//        broadcastGameList();
        return "onlineGame";
    }

    // ============ WebSocket Endpoints ============

    // WebSocket ход
    @MessageMapping("/online-move")
    public void handleOnlineMove(OnlineGameMessage msg) {
        service.makeMove(msg.getGameId(), msg.getNick(), msg.getRow(), msg.getCol());
        OnlineGame og = service.getOnlineGame(msg.getGameId());
        messagingTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);
    }

    @MessageMapping("/rematch")
    public void handleRematch(RematchMessage msg) {
        OnlineGame og = service.rematchGame(msg.getGameId());
        if (og!=null) {
            messagingTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);
        }
    }

    // WebSocket "leave-game"
    @MessageMapping("/leave-game")
    public void handleLeaveGame(LeaveGameMessage msg) {
        boolean closed = service.leaveGame(msg.getGameId(), msg.getNick());
        broadcastGameList();
        if(closed){
            messagingTemplate.convertAndSend("/topic/online-game-"+msg.getGameId(), "\"CLOSED\"");
        } else {
            // обновлённое состояние
            OnlineGame og = service.getOnlineGame(msg.getGameId());
            if (og!=null){
                messagingTemplate.convertAndSend("/topic/online-game-"+msg.getGameId(), og);
            }
        }
    }

    @GetMapping("/online-state")
    @ResponseBody
    public OnlineGame getOnlineState(@RequestParam("gameId") long gameId) {
        return service.getOnlineGame(gameId);
    }

    public void broadcastGameList() { //todo private?
        List<OnlineGame> allGames = service.listGames();
        messagingTemplate.convertAndSend("/topic/game-list", allGames);
    }
}