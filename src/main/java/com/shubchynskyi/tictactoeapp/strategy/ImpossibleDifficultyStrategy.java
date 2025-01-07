package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * God/Impossible difficulty - полный код, взятый
 * из вашего старого фрагмента (с приоритетами, уголками и т.д.).
 */
public class ImpossibleDifficultyStrategy implements DifficultyStrategy {
    private static final int FIELD_SIZE = 9;
    private static final int CENTER_CELL_INDEX = 4;
    private static final int[] PRIORITY_CELLS = {4, 0, 2, 6, 8};
    private static final int[] NOT_CORNER_CELLS = {1, 3, 5, 7};
    private static final int GOD_NUMBER = 3; // special logic

    @Override
    public void makeMove(Game game) {
        List<Integer> empties = getEmptyCells(game);

        // 1) если все пустые, AI=Cross => random priority
        if (empties.size() == FIELD_SIZE) {
            makeFirstMoveIfAiSignCross(game);
            return;
        }

        // 2) если осталось (FIELD_SIZE - GOD_NUMBER) ...
        if (empties.size() == (FIELD_SIZE - GOD_NUMBER)) {
            if (isPlayerInOppositeCorners(game) && isComputerInMiddle(game)) {
                makeMoveAvoidingCorners(game);
                return;
            }
        }

        // 3) Win
        int w = findWinningMove(game, game.getAiSign());
        if (w != -1) {
            game.doMove(w);
            return;
        }

        // 4) Block
        int b = findWinningMove(game, game.getPlayerSign());
        if (b != -1) {
            game.doMove(b);
            return;
        }

        // 5) Priority
        makePriorityMove(game);
    }



    private void makeFirstMoveIfAiSignCross(Game game) {
        if (game.getAiSign() == Sign.CROSS) { // TODO нужна ли проверка, если перед вызовом метода
            // todo проверяется что все пустые? Может независимо от знака ставить знак в приоритетные?
            // random priority todo может можно стримом выбрать незанятые и рандомно походить?
            List<Integer> cells = new ArrayList<>();
            for (int c: PRIORITY_CELLS) {
                if (game.getSignAt(c) == Sign.EMPTY) {
                    cells.add(c);
                }
            }
            if (!cells.isEmpty()) {
                int chosen = cells.get(new Random().nextInt(cells.size()));
                game.doMove(chosen);
            }
        } else {
            // AI = O => random empty
            List<Integer> empties = getEmptyCells(game);
            if(!empties.isEmpty()) {
                int cell = empties.get(new Random().nextInt(empties.size()));
                game.doMove(cell);
            }
        }
    }

    private boolean isPlayerInOppositeCorners(Game game) {
        Sign p = game.getPlayerSign();
        if (game.getSignAt(0)==p && game.getSignAt(8)==p) return true;
        if (game.getSignAt(2)==p && game.getSignAt(6)==p) return true;
        return false;
    }
    private boolean isComputerInMiddle(Game game) {
        return game.getSignAt(CENTER_CELL_INDEX) == game.getAiSign();
    }

    private void makeMoveAvoidingCorners(Game game) {
        List<Integer> moves = new ArrayList<>();
        for (int c: NOT_CORNER_CELLS) { // todo может можно стримом выбрать незанятые и рандомно походить?
            if (game.getSignAt(c) == Sign.EMPTY) {
                moves.add(c);
            }
        }
        if(!moves.isEmpty()) {
            int cell = moves.get(new Random().nextInt(moves.size()));
            game.doMove(cell);
        }
    }

    private int findWinningMove(Game game, Sign sign) {
        for (int i=0; i<FIELD_SIZE; i++){ // todo стрим?
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

    private void makePriorityMove(Game game) {
        // Center first // todo будет метод вынесенный из хард?
        if (game.getSignAt(CENTER_CELL_INDEX)==Sign.EMPTY) {
            game.doMove(CENTER_CELL_INDEX);
            return;
        }
        // else corners
        List<Integer> cornerCells = new ArrayList<>();
        for (int c: PRIORITY_CELLS) { // todo стрим?
            if (game.getSignAt(c) == Sign.EMPTY) {
                cornerCells.add(c);
            }
        }
        if(!cornerCells.isEmpty()) {
            int cell = cornerCells.get(new Random().nextInt(cornerCells.size()));
            game.doMove(cell);
            return;
        }
        // fallback
        List<Integer> empties = getEmptyCells(game);
        if(!empties.isEmpty()) {
            int e = empties.get(new Random().nextInt(empties.size()));
            game.doMove(e);
        }
    }

    private List<Integer> getEmptyCells(Game game) {
        List<Integer> list = new ArrayList<>();
        for(int i=0; i<FIELD_SIZE; i++){
            if (game.getSignAt(i)==Sign.EMPTY) {
                list.add(i);
            }
        }
        return list;
    }
}