package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Базовый абстрактный класс для стратегий.
 *
 * 1) final-метод makeMove(...) запрашивает у подкласса createSteps()
 *    (цепочку шагов).
 * 2) Каждый шаг (StrategyStep) вызывается по порядку.
 *    Если вернул true => ход сделан => выходим.
 *
 * 3) Храним логику "tryWin","tryBlock","tryCenter","trySpecialGod","doRandomMove"
 *    в protected. Если конкретной стратегии надо, она переопределяет их
 *    (или нет — берёт поведение базовое).
 *
 * 4) Утилитные методы (findWinningCell, getEmptyCells, etc.) делаем private,
 *    чтобы их не видно было извне,
 *    и чтобы потомки могли вызывать только через эти protected-методы.
 */
public abstract class AbstractDifficultyStrategy implements DifficultyStrategy {

    private static final int FIELD_SIZE = 9;
    private static final int CENTER_CELL_INDEX = 4;
    private static final int[] PRIORITY_CELLS = {4, 0, 2, 6, 8};
    private static final int[] NOT_CORNER_CELLS = {1, 3, 5, 7};
    private static final int GOD_NUMBER = 3; // special logic

    @Override
    public final void makeMove(Game game) {
        // получаем steps у потомка
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

    protected boolean tryPutSignInCenter(Game game) {
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

    //******** Private ********//

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
