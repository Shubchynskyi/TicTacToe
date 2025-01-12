package com.shubchynskyi.tictactoeapp.enums;

import lombok.Getter;

@Getter
public enum Sign {
    EMPTY(' '),
    CROSS('X'),
    NOUGHT('0');

    private final char sign;

    Sign(char sign) {
        this.sign = sign;
    }

}