package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.model.OnlineGame;
import com.shubchynskyi.tictactoeapp.service.OnlineGameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // Список игр (Thymeleaf)
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

        return "online"; // Thymeleaf template
    }

    // Создать игру
    @PostMapping("/create-online")
    public String createOnline(HttpSession session) {
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }
        // 1) Создаём игру => конструктор сразу ставит "playerX=creatorNick" или "playerO=creatorNick"
        long gameId = service.createGame(nick);
        service.joinGame(gameId, nick);

        // 2) НЕ вызываем joinGame для создателя! Он уже внутри (X или O).
        // 3) Запускаем 10-минутный таймер (так как пока 1 игрок).
        service.startInactivityTimer(gameId, messagingTemplate, 10);

        // 4) Рассылаем обновление списка
        broadcastGameList();

        // 5) Сразу переходим на /onlineGame
        return "redirect:/onlineGame?gameId=" + gameId;
    }

    @GetMapping("/join-online")
    public String joinGame(@RequestParam("gameId") long gameId, HttpSession session) {
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }

        // 1) Присоединяем второго (если "waitingForSecondPlayer")
        service.joinGame(gameId, nick);

        // 2) Оповестим создателя, чтобы у него отобразился nick второго
        OnlineGame og = service.getOnlineGame(gameId);
        if (og != null) {
            messagingTemplate.convertAndSend("/topic/online-game-"+gameId, og);

            // Если теперь 2 игрока => 3 мин, перезапустим таймер
            if (!og.isFinished() && og.getPlayerX()!=null && og.getPlayerO()!=null) {
                service.startInactivityTimer(gameId, messagingTemplate, 3);
            }
        }

        broadcastGameList();
        return "redirect:/onlineGame?gameId=" + gameId;
    }

    // Страница самой онлайн-игры
    @GetMapping("/onlineGame")
    public String onlineGamePage(@RequestParam("gameId") long gameId,
                                 Model model, HttpSession session) {
        OnlineGame og = service.getOnlineGame(gameId);
        if (og == null) {
            return "redirect:/online";
        }
        String nick = (String) session.getAttribute("nick");
        if (nick == null) {
            nick = "Guest" + (System.currentTimeMillis() % 1000);
            session.setAttribute("nick", nick);
        }

        // Если игра занята обоими, не даём 3-му
        if (og.getPlayerX() != null && og.getPlayerO() != null) {
            boolean isPlayer = nick.equals(og.getPlayerX())
                    || nick.equals(og.getPlayerO());
            if (!isPlayer) {
                return "redirect:/online";
            }
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
        if (og != null) {
            messagingTemplate.convertAndSend("/topic/online-game-" + msg.getGameId(), og);
        }
    }

    @MessageMapping("/rematch")
    public void handleRematch(RematchMessage msg) {
        long gameId = msg.getGameId();
        OnlineGame og = service.rematchGame(gameId);
        if (og != null) {
            messagingTemplate.convertAndSend("/topic/online-game-" + gameId, og);

            // Если 2 игрока => 3 min, иначе 10
            if (og.getPlayerX() != null && og.getPlayerO() != null && !og.isFinished()) {
                service.startInactivityTimer(gameId, messagingTemplate, 3);
            } else {
                service.startInactivityTimer(gameId, messagingTemplate, 10);
            }
        }
    }

    @MessageMapping("/leave-game")
    public void handleLeaveGame(LeaveGameMessage msg) {
        boolean closed = service.leaveGame(msg.getGameId(), msg.getNick());
        broadcastGameList();
        if (closed) {
            messagingTemplate.convertAndSend(
                    "/topic/online-game-" + msg.getGameId(), "\"CLOSED\"");
        } else {
            OnlineGame og = service.getOnlineGame(msg.getGameId());
            if (og != null) {
                messagingTemplate.convertAndSend(
                        "/topic/online-game-" + msg.getGameId(), og);
            }
        }
    }

    // Для onlineGame.html: начальное состояние
    @GetMapping("/online-state")
    @ResponseBody
    public OnlineGame getOnlineState(@RequestParam("gameId") long gameId) {
        return service.getOnlineGame(gameId);
    }

    public void broadcastGameList() {
        List<OnlineGame> allGames = service.listGames();
        messagingTemplate.convertAndSend("/topic/game-list", allGames);
    }
}
