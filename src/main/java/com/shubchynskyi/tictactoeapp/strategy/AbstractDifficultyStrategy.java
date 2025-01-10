package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public abstract class AbstractDifficultyStrategy implements DifficultyStrategy {

    private static final int FIELD_SIZE = 9;
    private static final int[] PRIORITY_CELLS = {4, 0, 2, 6, 8};
    private static final int CENTER_CELL_INDEX = 4;
    private static final int[] CORNER_CELLS = {0, 2, 6, 8};
    private static final int[] NOT_CORNER_CELLS = {1, 3, 5, 7};
    private static final int EMPTY_SIZE_FOR_CORNER_TRAP = 6; // special logic

    @Override
    public final void makeMove(Game game) {
        for (StrategyStep step : getSteps()) {
            boolean done = step.doStep(game);
            if (done) break;
        }
    }

    protected abstract List<StrategyStep> getSteps();

    protected boolean doRandomMove(Game game) {
        List<Integer> empties = getEmptyCells(game);
        if (!empties.isEmpty()) {
            int cell = empties.get(new Random().nextInt(empties.size()));
            game.doMove(cell);
            return true;
        }
        return false;
    }

    protected boolean tryToPutSignInCenter(Game game) {
        if (game.getSignAt(CENTER_CELL_INDEX) == Sign.EMPTY) {
            game.doMove(CENTER_CELL_INDEX);
            return true;
        }
        return false;
    }

    protected boolean tryToBlockPlayer(Game game) {
        int blockCell = findWinningCell(game, game.getPlayerSign());
        if (blockCell != -1) {
            game.doMove(blockCell);
            return true;
        }
        return false;
    }

    protected boolean tryToWin(Game game) {
        int winCell = findWinningCell(game, game.getAiSign());
        if (winCell != -1) {
            game.doMove(winCell);
            return true;
        }
        return false;
    }

    protected boolean tryToPriorityCell(Game game) {
        int emptySize = getEmptyCells(game).size();

        if (emptySize == FIELD_SIZE - 1) {
            return tryToPutSignInCenter(game) || tryToPutToPriorityCell(game, CORNER_CELLS);
        }

        return tryToPutToPriorityCell(game, PRIORITY_CELLS);
    }

    private boolean tryToPutToPriorityCell(Game game, int[] cellsWantToMove) {
        List<Integer> availableCells = new ArrayList<>();
        for (int cell : cellsWantToMove) {
            if (game.getSignAt(cell) == Sign.EMPTY) {
                availableCells.add(cell);
            }
        }
        if (!availableCells.isEmpty()) {
            int cell = availableCells.get(new Random().nextInt(availableCells.size()));
            game.doMove(cell);
            return true;
        }
        return false;
    }

    protected boolean tryToAvoidCornerTrap(Game game) {
        List<Integer> emptyCells = getEmptyCells(game);
        if (emptyCells.size() == EMPTY_SIZE_FOR_CORNER_TRAP) {
            if (isPlayerInOppositeCorners(game) && isAiInCenterCell(game)) {
                makeMoveAvoidingCorners(game);
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerInOppositeCorners(Game game) {
        Sign playerSign = game.getPlayerSign();
        if (game.getSignAt(0) == playerSign && game.getSignAt(8) == playerSign) return true;
        if (game.getSignAt(2) == playerSign && game.getSignAt(6) == playerSign) return true;
        return false;
    }

    private boolean isAiInCenterCell(Game game) {
        return game.getSignAt(CENTER_CELL_INDEX) == game.getAiSign();
    }

    private void makeMoveAvoidingCorners(Game game) {
        List<Integer> moves = new ArrayList<>();
        for (int c : NOT_CORNER_CELLS) {
            if (game.getSignAt(c) == Sign.EMPTY) {
                moves.add(c);
            }
        }
        if (!moves.isEmpty()) {
            int cell = moves.get(new Random().nextInt(moves.size()));
            game.doMove(cell);
        }
    }

    private int findWinningCell(Game game, Sign sign) {
        for (int i = 0; i < 9; i++) {
            if (game.getSignAt(i) == Sign.EMPTY) {
                game.setSignAt(i, sign);
                if (game.isWin(sign)) {
                    game.setSignAt(i, Sign.EMPTY);
                    return i;
                }
                game.setSignAt(i, Sign.EMPTY);
            }
        }
        return -1;
    }

    private List<Integer> getEmptyCells(Game game) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (game.getSignAt(i) == Sign.EMPTY) {
                list.add(i);
            }
        }
        return list;
    }

}