package com.shubchynskyi.tictactoeapp.model;


public class OnlineGame {

    private long gameId;
    private Game game; // внутри - поле 3x3
    private String playerX;
    private String playerO;
    private boolean waitingForSecondPlayer;
    private boolean finished;
    private String winnerNick;

    public OnlineGame() {
        // default for JSON
    }

    public OnlineGame(long gameId, String creatorNick) {
        this.gameId = gameId;
        // Создаём Game c default = single,
        // но, если хотим "online" - можно поменять:
        this.game = new Game("online", "X", "easy");
        this.playerX = creatorNick;
        this.playerO = null;
        this.waitingForSecondPlayer = true;
        this.finished = false;
        this.winnerNick = null;
    }

    public long getGameId() { return gameId; }
    public Game getGame() { return game; }
    public String getPlayerX() { return playerX; }
    public void setPlayerX(String px) { playerX=px; }
    public String getPlayerO() { return playerO; }
    public void setPlayerO(String po) { playerO=po; }
    public boolean isWaitingForSecondPlayer() { return waitingForSecondPlayer; }
    public void setWaitingForSecondPlayer(boolean w) { waitingForSecondPlayer=w; }
    public boolean isFinished() { return finished; }
    public void setFinished(boolean f) { finished=f; }
    public String getWinnerNick() { return winnerNick; }
    public void setWinnerNick(String wn) { winnerNick=wn; }
}