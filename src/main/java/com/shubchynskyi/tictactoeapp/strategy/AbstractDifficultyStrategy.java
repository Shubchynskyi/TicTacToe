package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.enums.Sign;
import com.shubchynskyi.tictactoeapp.domain.Game;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractDifficultyStrategy implements DifficultyStrategy {

    private static final int FIELD_SIZE = 9;
    private static final List<Integer> PRIORITY_CELLS = List.of(4, 0, 2, 6, 8);
    private static final int CENTER_CELL_INDEX = 4;
    private static final List<Integer> CORNER_CELLS = List.of(0, 2, 6, 8);
    private static final List<Integer> NOT_CORNER_CELLS = List.of(1, 3, 5, 7);
    private static final int EMPTY_SIZE_FOR_CORNER_TRAP = 6; // special logic

    private final Random random = new Random();
    @Override
    public final void makeMove(Game game) {
        for (StrategyStep step : getSteps()) {
            if (step.doStep(game)) {
                break;
            }
        }
    }

    protected abstract List<StrategyStep> getSteps();

    protected boolean doRandomMove(Game game) {
        return placeRandomMove(game, getEmptyCells(game));
    }

    protected boolean tryToPutSignInCenter(Game game) {
        if (isCellEmpty(game, CENTER_CELL_INDEX)) {
            game.doMove(CENTER_CELL_INDEX);
            return true;
        }
        return false;
    }

    protected boolean tryToBlockPlayer(Game game) {
        return tryToCompleteLine(game, game.getPlayerSign());
    }

    protected boolean tryToWin(Game game) {
        return tryToCompleteLine(game, game.getAiSign());
    }

    protected boolean tryToPriorityCell(Game game) {
        int emptySize = getEmptyCells(game).size();
        if (emptySize == FIELD_SIZE - 1) {
            return tryToPutSignInCenter(game) || tryToPutToPriorityCell(game, CORNER_CELLS);
        }
        return tryToPutToPriorityCell(game, PRIORITY_CELLS);
    }

    protected boolean tryToAvoidCornerTrap(Game game) {
        if (getEmptyCells(game).size() == EMPTY_SIZE_FOR_CORNER_TRAP &&
                isPlayerInOppositeCorners(game) &&
                isAiInCenterCell(game)) {
            return tryToPutToPriorityCell(game, NOT_CORNER_CELLS);
        }
        return false;
    }

    private boolean tryToCompleteLine(Game game, Sign sign) {
        int cellIndex = findWinningCell(game, sign);
        if (cellIndex != -1) {
            game.doMove(cellIndex);
            return true;
        }
        return false;
    }

    private boolean tryToPutToPriorityCell(Game game, List<Integer> cells) {
        List<Integer> availableCells = filterEmptyCells(game, cells);
        return placeRandomMove(game, availableCells);
    }

    private boolean placeRandomMove(Game game, List<Integer> availableCells) {
        if (!availableCells.isEmpty()) {
            int randomCell = availableCells.get(random.nextInt(availableCells.size()));
            game.doMove(randomCell);
            return true;
        }
        return false;
    }

    private List<Integer> filterEmptyCells(Game game, List<Integer> cells) {
        return cells.stream()
                .filter(cell -> isCellEmpty(game, cell))
                .collect(Collectors.toList());
    }

    private boolean isPlayerInOppositeCorners(Game game) {
        Sign playerSign = game.getPlayerSign();
        return (isCellSign(game, 0, playerSign) && isCellSign(game, 8, playerSign)) ||
                (isCellSign(game, 2, playerSign) && isCellSign(game, 6, playerSign));
    }

    private boolean isAiInCenterCell(Game game) {
        return isCellSign(game, CENTER_CELL_INDEX, game.getAiSign());
    }

    private int findWinningCell(Game game, Sign sign) {
        return IntStream.range(0, FIELD_SIZE)
                .filter(i -> isCellEmpty(game, i))
                .filter(i -> {
                    game.setSignAt(i, sign);
                    boolean isWinning = game.isWin(sign);
                    game.setSignAt(i, Sign.EMPTY);
                    return isWinning;
                })
                .findFirst()
                .orElse(-1);
    }

    private List<Integer> getEmptyCells(Game game) {
        return IntStream.range(0, FIELD_SIZE)
                .filter(i -> isCellEmpty(game, i))
                .boxed()
                .collect(Collectors.toList());
    }

    private boolean isCellEmpty(Game game, int cell) {
        return game.getSignAt(cell) == Sign.EMPTY;
    }

    private boolean isCellSign(Game game, int cell, Sign sign) {
        return game.getSignAt(cell) == sign;
    }
}