package com.shubchynskyi.tictactoeapp.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnlineGame {

    private long gameId;
    private Game game;

    private String playerXId;
    private String playerOId;
    private String playerXDisplay;
    private String playerODisplay;

    private String creatorId;
    private String creatorDisplay;

    private boolean waitingForSecondPlayer;
    private boolean finished;
    private String winnerDisplay;

    private int scoreX;
    private int scoreO;

    // Новое поле: когда истечёт время (в мс, System.currentTimeMillis())
    // Если 0 — значит таймер не активен
    private long closeTimeMillis; // todo удалить после тестов

    public OnlineGame() {
    }

    public OnlineGame(long gameId, String creatorId, String creatorDisplay) {
        this.gameId = gameId;
        this.creatorId = creatorId;
        this.creatorDisplay = creatorDisplay;

        boolean creatorIsX = (Math.random() < 0.5);
        if (creatorIsX) {
            this.game = new Game("online", "X", "easy");
            this.playerXId = creatorId;
            this.playerXDisplay = creatorDisplay;
            this.playerOId = null;
            this.playerODisplay = null;
        } else {
            this.game = new Game("online", "O", "easy");
            this.playerXId = null;
            this.playerXDisplay = null;
            this.playerOId = creatorId;
            this.playerODisplay = creatorDisplay;
        }

        this.waitingForSecondPlayer = true;
        this.finished = false;
        this.winnerDisplay = null;
        this.scoreX = 0;
        this.scoreO = 0;
        this.closeTimeMillis = 0;
    }
}
