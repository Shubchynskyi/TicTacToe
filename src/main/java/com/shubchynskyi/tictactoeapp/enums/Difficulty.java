package com.shubchynskyi.tictactoeapp.enums;

import lombok.Getter;

@Getter
public enum Difficulty {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard"),
    IMPOSSIBLE("impossible");

    private final String value;

    Difficulty(String value) {
        this.value = value;
    }

}