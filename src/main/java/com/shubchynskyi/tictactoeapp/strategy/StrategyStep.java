package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.model.Game;

@FunctionalInterface
public interface StrategyStep {
    boolean doStep(Game game);
}