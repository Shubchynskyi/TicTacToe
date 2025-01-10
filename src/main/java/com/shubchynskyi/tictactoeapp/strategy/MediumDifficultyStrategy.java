package com.shubchynskyi.tictactoeapp.strategy;

import java.util.List;


public class MediumDifficultyStrategy extends AbstractDifficultyStrategy {
    private final List<StrategyStep> steps;

    public MediumDifficultyStrategy() {
        super();

        this.steps = List.of(
                this::tryToWin,
                this::tryToBlockPlayer,
                this::doRandomMove
        );
    }

    @Override
    protected List<StrategyStep> getSteps() {
        return steps;
    }
}