package com.shubchynskyi.tictactoeapp.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Route {

    public static final String INDEX = "/";
    public static final String START_GAME = "/start";

    public static final String GAME_STATE = "/game-state";
    public static final String MAKE_MOVE = "/make-move";
    public static final String RESTART_LOCAL = "/restart-local";

    public static final String UPDATE_NICK = "/update-nick";
    public static final String ONLINE = "/online";
    public static final String CREATE_ONLINE = "/create-online";
    public static final String JOIN_ONLINE = "/join-online";
    public static final String ONLINE_GAME = "/onlineGame";
    public static final String REDIRECT = "redirect:";
    public static final String GAME_ID_PARAM = "?gameId=";

    public static final String ONLINE_MOVE = "/online-move";
    public static final String REMATCH = "/rematch";
    public static final String LEAVE_GAME = "/leave-game";
    public static final String ONLINE_STATE = "/online-state";

    public static final String TOPIC_ONLINE_GAME_PREFIX = "/topic/online-game-";
    public static final String TOPIC_GAME_LIST = "/topic/game-list";

}