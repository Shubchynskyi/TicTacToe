package com.shubchynskyi.tictactoeapp.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Route {
    public static final String ONLINE = "/online";
    public static final String CREATE_ONLINE = "/create-online";
    public static final String JOIN_ONLINE = "/join-online";
    public static final String ONLINE_GAME = "/onlineGame";
    public static final String ONLINE_MOVE = "/online-move";
    public static final String REMATCH = "/rematch";
    public static final String LEAVE_GAME = "/leave-game";
    public static final String ONLINE_STATE = "/online-state";
    public static final String TOPIC_ONLINE_GAME_PREFIX = "/topic/online-game-";
    public static final String TOPIC_GAME_LIST = "/topic/game-list";
    public static final String REDIRECT = "redirect:";
    public static final String GAME_ID_PATTERN = "?" + Key.GAME_ID + "=";
    public static final String INDEX = "/"; // Константа для маршрута "/"
    public static final String GAME_STATE = "/game-state"; // Константа для маршрута "/game-state"
    public static final String MAKE_MOVE = "/make-move"; // Константа для маршрута "/make-move"
    public static final String RESTART_LOCAL = "/restart-local";
}