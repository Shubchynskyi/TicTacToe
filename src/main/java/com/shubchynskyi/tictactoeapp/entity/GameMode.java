package com.shubchynskyi.tictactoeapp.entity;


public enum GameMode {
    SINGLE_PLAYER("single"),
    MULTIPLAYER("multiplayer");

    private final String value;

    GameMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static GameMode fromString(String mode) {
        for (GameMode gm : GameMode.values()) {
            if (gm.value.equalsIgnoreCase(mode)) {
                return gm;
            }
        }
        throw new IllegalArgumentException("Unknown game mode: " + mode);
    }
}