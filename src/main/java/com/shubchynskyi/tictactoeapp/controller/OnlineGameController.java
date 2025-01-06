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
import java.util.UUID;

@Controller
public class OnlineGameController {

    private final OnlineGameService service;
    private final SimpMessagingTemplate msgTemplate;

    @Autowired
    public OnlineGameController(OnlineGameService service,
                                SimpMessagingTemplate msgTemplate) {
        this.service = service;
        this.msgTemplate = msgTemplate;
    }

    @GetMapping("/online")
    public String showOnlineGames(Model model, HttpSession session) {
        List<OnlineGame> games = service.listGames();
        model.addAttribute("games", games);

        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            session.setAttribute("userId", userId);
        }
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
        String userId = (String) session.getAttribute("userId");
        String nick = (String) session.getAttribute("nick");
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            session.setAttribute("userId", userId);
        }
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }

        long gameId = service.createGame(userId, nick);
        service.startInactivityTimer(gameId, msgTemplate, 10);

        broadcastGameList();
        return "redirect:/onlineGame?gameId=" + gameId;
    }

    @GetMapping("/join-online")
    public String joinGame(@RequestParam("gameId") long gameId, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String nick = (String) session.getAttribute("nick");
        service.joinGame(gameId, userId, nick);

        // достаём og
        OnlineGame og = service.getOnlineGame(gameId);
        if (og != null && !og.isFinished()) {
            if (og.getPlayerXId() != null && og.getPlayerOId() != null) {
                // 2 игрока => 3 мин
                service.startInactivityTimer(gameId, msgTemplate, 3);
            }
            // шлём обновлённое state
            msgTemplate.convertAndSend("/topic/online-game-" + gameId, og);
        }

        broadcastGameList();
        return "redirect:/onlineGame?gameId=" + gameId;
    }

    @GetMapping("/onlineGame")
    public String onlineGamePage(@RequestParam("gameId") long gameId,
                                 Model model, HttpSession session) {
        OnlineGame og = service.getOnlineGame(gameId);
        if (og == null) {
            return "redirect:/online";
        }
        String userId = (String) session.getAttribute("userId");
        String nick = (String) session.getAttribute("nick");
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            session.setAttribute("userId", userId);
        }
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }
        if (!og.isWaitingForSecondPlayer()) {
            boolean isPlayer = userId.equals(og.getPlayerXId())
                    || userId.equals(og.getPlayerOId());
            if (!isPlayer) {
                return "redirect:/online";
            }
        }

        model.addAttribute("onlineGame", og);
        model.addAttribute("nick", nick);
        return "onlineGame";
    }

    @MessageMapping("/online-move")
    public void handleOnlineMove(OnlineGameMessage msg) {
        service.makeMove(msg.getGameId(), msg.getUserId(), msg.getRow(), msg.getCol());
        OnlineGame og = service.getOnlineGame(msg.getGameId());
        if (og != null) {
            msgTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);
        }
    }

    @MessageMapping("/rematch")
    public void handleRematch(RematchMessage msg) {
        OnlineGame og = service.rematchGame(msg.getGameId());
        if (og != null) {
            msgTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);
            if (og.getPlayerXId() != null && og.getPlayerOId() != null && !og.isFinished()) {
                service.startInactivityTimer(msg.getGameId(), msgTemplate, 3);
            } else {
                service.startInactivityTimer(msg.getGameId(), msgTemplate, 10);
            }
        }
    }

    @MessageMapping("/leave-game")
    public void handleLeaveGame(LeaveGameMessage msg) {
        boolean closed = service.leaveGame(msg.getGameId(), msg.getUserId());
        broadcastGameList();
        if (closed) {
            msgTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), "\"CLOSED\"");
        } else {
            OnlineGame og = service.getOnlineGame(msg.getGameId());
            if (og != null) {
                // шлём обновлённое состояние
                msgTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);

                // ========== ДОБАВЛЯЕМ ЛОГИКУ (1 игрок => 10 мин) ==========
                // if "og.getPlayerXId()!=null ^ og.getPlayerOId()==null" или наоборот
                // => 1 игрок => 10 мин
                boolean onePlayerLeft =
                        ( (og.getPlayerXId()!=null && og.getPlayerOId()==null)
                                || (og.getPlayerXId()==null && og.getPlayerOId()!=null) );
                if (!og.isFinished() && onePlayerLeft) {
                    // Запускаем 10-минутный таймер
                    service.startInactivityTimer(msg.getGameId(), msgTemplate, 10);
                }
            }
        }
    }

    @GetMapping("/online-state")
    @ResponseBody
    public OnlineGame getOnlineState(@RequestParam("gameId") long gameId) {
        return service.getOnlineGame(gameId);
    }

    public void broadcastGameList() {
        List<OnlineGame> all = service.listGames();
        msgTemplate.convertAndSend("/topic/game-list", all);
    }
}