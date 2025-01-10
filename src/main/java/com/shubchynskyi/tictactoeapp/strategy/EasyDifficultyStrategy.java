package com.shubchynskyi.tictactoeapp.strategy;

import java.util.List;


public class EasyDifficultyStrategy extends AbstractDifficultyStrategy {
    private final List<StrategyStep> steps;

    public EasyDifficultyStrategy() {
        super();

        this.steps = List.of(
                this::doRandomMove
        );
    }

    @Override
    protected List<StrategyStep> getSteps() {
        return steps;
    }
}