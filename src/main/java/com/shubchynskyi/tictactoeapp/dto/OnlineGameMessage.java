package com.shubchynskyi.tictactoeapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnlineGameMessage {
    private long gameId;
    private String userId;
    private int row;
    private int col;

    public OnlineGameMessage() {} //todo need it? for json?
}
