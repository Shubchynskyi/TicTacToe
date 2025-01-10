package com.shubchynskyi.tictactoeapp.strategy;

import java.util.List;


public class ImpossibleDifficultyStrategy extends AbstractDifficultyStrategy {
    private final List<StrategyStep> steps;

    public ImpossibleDifficultyStrategy() {
        super();

        this.steps = List.of(
                this::tryToWin,
                this::tryToBlockPlayer,
                this::tryToAvoidCornerTrap,
                this::tryToPriorityCell,
                this::doRandomMove
        );
    }

    @Override
    protected List<StrategyStep> getSteps() {
        return steps;
    }
}