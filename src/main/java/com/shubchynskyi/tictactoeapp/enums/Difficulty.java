package com.shubchynskyi.tictactoeapp.enums;

import lombok.Getter;

@Getter
public enum Difficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    IMPOSSIBLE("Impossible");

    private final String value;

    Difficulty(String value) {
        this.value = value;
    }

}