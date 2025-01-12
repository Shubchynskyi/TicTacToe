package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.domain.Game;

@FunctionalInterface
public interface StrategyStep {
    boolean doStep(Game game);
}