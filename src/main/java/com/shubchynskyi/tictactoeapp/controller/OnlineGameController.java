package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.Key;
import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.domain.OnlineGame;
import com.shubchynskyi.tictactoeapp.dto.LeaveGameMessage;
import com.shubchynskyi.tictactoeapp.dto.OnlineGameMessage;
import com.shubchynskyi.tictactoeapp.dto.RematchMessage;
import com.shubchynskyi.tictactoeapp.service.OnlineGameService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class OnlineGameController {

    private final OnlineGameService onlineGameService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.defaultNick}")
    private String defaultNick;

    @Value("${app.timeOut.newGame}")
    private int timeOutForNewGame;
    @Value("${app.timeOut.ongoingGame}")
    private int timeOutForOngoingGame;

    @GetMapping(Route.ONLINE)
    public String showOnlineGames(Model model, HttpSession session) {
        List<OnlineGame> onlineGames = onlineGameService.listGames();
        model.addAttribute(Key.GAMES, onlineGames);

        ensureSessionAttributes(session);
        return Route.ONLINE;
    }

    @PostMapping(Route.CREATE_ONLINE)
    public String createOnline(HttpSession session) {
        ensureSessionAttributes(session);

        String userId = (String) session.getAttribute(Key.USER_ID);
        String nick = (String) session.getAttribute(Key.NICK);

        long gameId = onlineGameService.createGame(userId, nick);
        onlineGameService.startInactivityTimer(gameId, messagingTemplate, timeOutForNewGame);
        broadcastGameList();

        return Route.REDIRECT + Route.ONLINE_GAME + Route.GAME_ID_PATTERN + gameId;
    }

    @GetMapping(Route.JOIN_ONLINE)
    public String joinGame(@RequestParam(Key.GAME_ID) long gameId, HttpSession session) {
        ensureSessionAttributes(session);

        String userId = (String) session.getAttribute(Key.USER_ID);
        String nick = (String) session.getAttribute(Key.NICK);

        onlineGameService.joinGame(gameId, userId, nick);
        handleGameJoin(gameId);

        broadcastGameList();
        return Route.REDIRECT + Route.ONLINE_GAME + Route.GAME_ID_PATTERN + gameId;
    }

    @GetMapping(Route.ONLINE_GAME)
    public String onlineGamePage(@RequestParam(Key.GAME_ID) long gameId, Model model, HttpSession session) {
        ensureSessionAttributes(session);

        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        if (onlineGame == null) {
            return Route.REDIRECT + Route.ONLINE;
        }

        String userId = (String) session.getAttribute(Key.USER_ID);
        String nick = (String) session.getAttribute(Key.NICK);

        if (!onlineGame.isWaitingForSecondPlayer()) {
            boolean isPlayer = userId.equals(onlineGame.getPlayerXId()) || userId.equals(onlineGame.getPlayerOId());
            if (!isPlayer) {
                return Route.REDIRECT + Route.ONLINE;
            }
        }

        model.addAttribute(Key.ONLINE_GAME, onlineGame);
        model.addAttribute(Key.NICK, nick);
        return Key.ONLINE_GAME;
    }

    @MessageMapping(Route.ONLINE_MOVE)
    public void handleOnlineMove(OnlineGameMessage message) {
        onlineGameService.makeMove(message.getGameId(), message.getUserId(), message.getRow(), message.getCol());

        OnlineGame onlineGame = onlineGameService.getOnlineGame(message.getGameId());
        if (onlineGame != null) {
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), onlineGame);
        }
    }

    @MessageMapping(Route.REMATCH)
    public void handleRematch(RematchMessage message) {
        OnlineGame onlineGame = onlineGameService.rematchGame(message.getGameId());
        if (onlineGame != null) {
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), onlineGame);

            if (onlineGame.getPlayerXId() != null && onlineGame.getPlayerOId() != null && !onlineGame.isFinished()) {
                onlineGameService.startInactivityTimer(message.getGameId(), messagingTemplate, timeOutForOngoingGame);
            } else {
                onlineGameService.startInactivityTimer(message.getGameId(), messagingTemplate, timeOutForNewGame);
            }
        }
    }

    @MessageMapping(Route.LEAVE_GAME)
    public void handleLeaveGame(LeaveGameMessage message) {
        boolean gameClosed = onlineGameService.leaveGame(message.getGameId(), message.getUserId());
        broadcastGameList();

        if (gameClosed) {
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), Key.CLOSED);
        } else {
            OnlineGame onlineGame = onlineGameService.getOnlineGame(message.getGameId());
            if (onlineGame != null) {
                messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), onlineGame);

                if ((onlineGame.getPlayerXId() != null && onlineGame.getPlayerOId() == null)
                        || (onlineGame.getPlayerXId() == null && onlineGame.getPlayerOId() != null)) {
                    if (!onlineGame.isFinished()) {
                        onlineGameService.startInactivityTimer(message.getGameId(), messagingTemplate, timeOutForNewGame);
                    }
                }
            }
        }
    }

    @GetMapping(Route.ONLINE_STATE)
    @ResponseBody
    public OnlineGame getOnlineState(@RequestParam(Key.GAME_ID) long gameId) {
        return onlineGameService.getOnlineGame(gameId);
    }

    private void ensureSessionAttributes(HttpSession session) {
        if (session.getAttribute(Key.USER_ID) == null) {
            String userId = UUID.randomUUID().toString();
            session.setAttribute(Key.USER_ID, userId);
        }

        if (session.getAttribute(Key.NICK) == null) {
            String nick = defaultNick + (System.currentTimeMillis() % 1000);
            session.setAttribute(Key.NICK, nick);
        }
    }

    private void handleGameJoin(long gameId) {
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        if (onlineGame != null && !onlineGame.isFinished()) {
            if (onlineGame.getPlayerXId() != null && onlineGame.getPlayerOId() != null) {
                onlineGameService.startInactivityTimer(gameId, messagingTemplate, timeOutForOngoingGame);
            }
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + gameId, onlineGame);
        }
    }

    private void broadcastGameList() {
        List<OnlineGame> allGames = onlineGameService.listGames();
        messagingTemplate.convertAndSend(Route.TOPIC_GAME_LIST, allGames);
    }
}