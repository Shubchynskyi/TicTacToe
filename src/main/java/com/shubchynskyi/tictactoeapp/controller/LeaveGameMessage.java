package com.shubchynskyi.tictactoeapp.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveGameMessage {
    private long gameId;
    private String nick;
}