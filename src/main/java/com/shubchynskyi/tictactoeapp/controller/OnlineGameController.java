package com.shubchynskyi.tictactoeapp.controller;

import com.shubchynskyi.tictactoeapp.constants.*;
import com.shubchynskyi.tictactoeapp.domain.OnlineGame;
import com.shubchynskyi.tictactoeapp.dto.LeaveGameMessage;
import com.shubchynskyi.tictactoeapp.dto.OnlineGameMessage;
import com.shubchynskyi.tictactoeapp.dto.RematchMessage;
import com.shubchynskyi.tictactoeapp.service.OnlineGameService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.function.Supplier;

@Slf4j
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
        log.info("Fetching list of online games.");
        List<OnlineGame> onlineGames = onlineGameService.listGames();
        model.addAttribute(SessionAttributes.GAMES, onlineGames);
        ensureSessionAttributes(session);
        return View.ONLINE;
    }

    @PostMapping(Route.CREATE_ONLINE)
    public String createOnline(HttpSession session) {
        ensureSessionAttributes(session);
        String userId = (String) session.getAttribute(SessionAttributes.USER_ID);
        String nick = (String) session.getAttribute(SessionAttributes.NICK);

        log.info("Creating new online game for user: {}, nick: {}", userId, nick);

        long gameId = onlineGameService.createGame(userId, nick);
        onlineGameService.startInactivityTimer(gameId, messagingTemplate, timeOutForNewGame);
        broadcastGameList();

        log.info("New online game created with ID: {}", gameId);
        return Route.REDIRECT + Route.ONLINE_GAME + Route.GAME_ID_PARAM + gameId;
    }

    @GetMapping(Route.JOIN_ONLINE)
    public String joinGame(@RequestParam(RequestParams.GAME_ID) long gameId, HttpSession session) {
        ensureSessionAttributes(session);
        String userId = (String) session.getAttribute(SessionAttributes.USER_ID);
        String nick = (String) session.getAttribute(SessionAttributes.NICK);

        log.info("User {} ({}) is joining game {}", userId, nick, gameId);

        onlineGameService.joinGame(gameId, userId, nick);
        handleGameJoin(gameId);
        broadcastGameList();

        log.info("User {} joined game {}", userId, gameId);
        return Route.REDIRECT + Route.ONLINE_GAME + Route.GAME_ID_PARAM + gameId;
    }

    @GetMapping(Route.ONLINE_GAME)
    public String onlineGamePage(@RequestParam(RequestParams.GAME_ID) long gameId, Model model, HttpSession session) {
        log.info("Loading online game page for gameId: {}", gameId);
        ensureSessionAttributes(session);

        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        if (onlineGame == null) {
            log.warn("Game with ID {} not found, redirecting to online page.", gameId);
            return Route.REDIRECT + Route.ONLINE;
        }

        String userId = (String) session.getAttribute(SessionAttributes.USER_ID);

        if (!onlineGame.isWaitingForSecondPlayer() &&
                !(userId.equals(onlineGame.getPlayerXId()) || userId.equals(onlineGame.getPlayerOId()))) {
            log.warn("User {} is not allowed to join game {}, redirecting.", userId, gameId);
            return Route.REDIRECT + Route.ONLINE;
        }

        model.addAttribute(SessionAttributes.ONLINE_GAME, onlineGame);
        model.addAttribute(SessionAttributes.NICK, session.getAttribute(SessionAttributes.NICK));

        log.info("Returning View.ONLINE_GAME for gameId: {}", gameId);
        return View.ONLINE_GAME;
    }

    @MessageMapping(Route.ONLINE_MOVE)
    public void handleOnlineMove(OnlineGameMessage message) {
        log.info("Processing move in game {} by user {}: row {}, col {}",
                message.getGameId(), message.getUserId(), message.getRow(), message.getCol());

        onlineGameService.makeMove(message.getGameId(), message.getUserId(), message.getRow(), message.getCol());
        OnlineGame onlineGame = onlineGameService.getOnlineGame(message.getGameId());

        if (onlineGame != null) {
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), onlineGame);
            log.info("Move processed. Updated game state sent.");
        }
    }

    @MessageMapping(Route.REMATCH)
    public void handleRematch(RematchMessage message) {
        log.info("Processing rematch request for game {}", message.getGameId());

        OnlineGame onlineGame = onlineGameService.rematchGame(message.getGameId());
        if (onlineGame != null) {
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), onlineGame);
            int timeout = (onlineGame.getPlayerXId() != null && onlineGame.getPlayerOId() != null && !onlineGame.isFinished())
                    ? timeOutForOngoingGame
                    : timeOutForNewGame;
            onlineGameService.startInactivityTimer(message.getGameId(), messagingTemplate, timeout);

            log.info("Rematch started for game {}", message.getGameId());
        }
    }

    @MessageMapping(Route.LEAVE_GAME)
    public void handleLeaveGame(LeaveGameMessage message) {
        log.info("User {} is leaving game {}", message.getUserId(), message.getGameId());

        boolean gameClosed = onlineGameService.leaveGame(message.getGameId(), message.getUserId());
        broadcastGameList();

        if (gameClosed) {
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), WebSocketCommand.CLOSED);
            log.info("Game {} closed due to player leaving.", message.getGameId());
        } else {
            OnlineGame onlineGame = onlineGameService.getOnlineGame(message.getGameId());
            if (onlineGame != null) {
                messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + message.getGameId(), onlineGame);
                log.info("Updated game state sent after player left.");
                if (isOnePlayerPresent(onlineGame) && !onlineGame.isFinished()) {
                    onlineGameService.startInactivityTimer(message.getGameId(), messagingTemplate, timeOutForNewGame);
                }
            }
        }
    }

    @GetMapping(Route.ONLINE_STATE)
    @ResponseBody
    public OnlineGame getOnlineState(@RequestParam(RequestParams.GAME_ID) long gameId) {
        log.info("Fetching state for game {}", gameId);
        return onlineGameService.getOnlineGame(gameId);
    }

    private void ensureSessionAttributes(HttpSession session) {
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

    private void handleGameJoin(long gameId) {
        OnlineGame onlineGame = onlineGameService.getOnlineGame(gameId);
        if (onlineGame != null && !onlineGame.isFinished()) {
            log.info("Handling game join for game {}", gameId);
            if (onlineGame.getPlayerXId() != null && onlineGame.getPlayerOId() != null) {
                onlineGameService.startInactivityTimer(gameId, messagingTemplate, timeOutForOngoingGame);
            }
            messagingTemplate.convertAndSend(Route.TOPIC_ONLINE_GAME_PREFIX + gameId, onlineGame);
        }
    }

    private void broadcastGameList() {
        log.info("Broadcasting updated game list.");
        List<OnlineGame> allGames = onlineGameService.listGames();
        messagingTemplate.convertAndSend(Route.TOPIC_GAME_LIST, allGames);
    }

    private boolean isOnePlayerPresent(OnlineGame onlineGame) {
        return (onlineGame.getPlayerXId() != null && onlineGame.getPlayerOId() == null)
                || (onlineGame.getPlayerXId() == null && onlineGame.getPlayerOId() != null);
    }
}