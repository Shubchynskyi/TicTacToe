package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Medium - проверяет выигрышный ход, блокировку,
 * иначе делает случайный ход из пустых.
 */
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