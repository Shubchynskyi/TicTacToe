package com.shubchynskyi.tictactoeapp.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RematchMessage {
    private long gameId;
    private String nick;

}