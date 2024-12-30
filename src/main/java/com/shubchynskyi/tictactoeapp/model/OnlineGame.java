package com.shubchynskyi.tictactoeapp.model;


public class OnlineGame {

    private long gameId;
    private Game game;
    private String playerX;
    private String playerO;
    private boolean waitingForSecondPlayer;
    private boolean finished;
    private String winnerNick;

    private int scoreX; // сколько побед у X
    private int scoreO; // сколько побед у O
    private String creatorNick;

    public OnlineGame() {
        // default
    }

    public OnlineGame(long gameId, String creatorNick) {
        this.gameId = gameId;
        this.creatorNick = creatorNick; // <--- ADD

        boolean creatorIsX = (Math.random() < 0.5);
        if (creatorIsX) {
            this.game = new Game("online", "X", "easy");
            this.playerX = creatorNick;
            this.playerO = null;
        } else {
            this.game = new Game("online", "O", "easy");
            this.playerX = null;
            this.playerO = creatorNick;
        }
        this.waitingForSecondPlayer = true;
        this.finished = false;
        this.winnerNick = null;
        this.scoreX = 0;
        this.scoreO = 0;
    }

    // getters / setters
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
    public void setWinnerNick(String w) { winnerNick=w; }

    public int getScoreX() { return scoreX; }
    public int getScoreO() { return scoreO; }
    public void setScoreX(int sx) { scoreX=sx; }
    public void setScoreO(int so) { scoreO=so; }

    public void setGame(Game g) {
        this.game = g;
    }

    public String getCreatorNick() {
        return creatorNick;
    }
    public void setCreatorNick(String c) { this.creatorNick=c; }
}