package com.shubchynskyi.tictactoeapp.model;


// Comments in English: This class wraps the Game object for online mode.
// It has an ID, two players' nicknames, and a single shared Game instance.
public class OnlineGame {
    private long gameId;
    private Game game;      // shared Tic-Tac-Toe state
    private String playerX; // nickname of the player using X
    private String playerO; // nickname of the player using O
    private boolean waitingForSecondPlayer;

    public OnlineGame() {
        // Default constructor
    }

    public OnlineGame(long gameId, String creatorNick) {
        this.gameId = gameId;
        // We'll create a new Game with mode = "online"
        // Let's assume the creator is X
        this.game = new Game("online", "X", null);
        this.playerX = creatorNick;
        this.playerO = null; // not joined yet
        this.waitingForSecondPlayer = true;
    }

    // Getters / Setters
    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getPlayerX() {
        return playerX;
    }

    public void setPlayerX(String playerX) {
        this.playerX = playerX;
    }

    public String getPlayerO() {
        return playerO;
    }

    public void setPlayerO(String playerO) {
        this.playerO = playerO;
    }

    public boolean isWaitingForSecondPlayer() {
        return waitingForSecondPlayer;
    }

    public void setWaitingForSecondPlayer(boolean waitingForSecondPlayer) {
        this.waitingForSecondPlayer = waitingForSecondPlayer;
    }
}
