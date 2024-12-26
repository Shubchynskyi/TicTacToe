package com.shubchynskyi.tictactoeapp.controller;


// Comments in English: a simple DTO for passing moves via WebSocket
public class OnlineGameMessage {
    private long gameId;
    private String nick;
    private int row;
    private int col;

    public OnlineGameMessage() {}

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
