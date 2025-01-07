package com.shubchynskyi.tictactoeapp.strategy;

import java.util.List;


public class HardDifficultyStrategy extends AbstractDifficultyStrategy {
    private final List<StrategyStep> steps;

    public HardDifficultyStrategy() {
        super();

        this.steps = List.of(
                this::tryToWin,
                this::tryToBlockPlayer,
                this::tryPutSignInCenter,
                this::doRandomMove
        );
    }

    @Override
    protected List<StrategyStep> getSteps() {
        return steps;
    }
}