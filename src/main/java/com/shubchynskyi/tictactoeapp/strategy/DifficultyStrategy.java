package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.domain.Game;

public interface DifficultyStrategy {
    void makeMove(Game game);
}