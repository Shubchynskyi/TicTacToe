package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Hard - дополнительно предпочитает центр, приоритетные клетки,
 * проверяет выигрышный ход и блокировку.
 */
public class HardDifficultyStrategy implements DifficultyStrategy {
    private static final int CENTER_INDEX = 4;

    @Override
    public void makeMove(Game game) {
        Sign aiSign = game.getAiSign();
        Sign playerSign = game.getPlayerSign();

        // 1) Win
        int w = findWinningCell(game, aiSign);
        if (w != -1) {
            game.doMove(w);
            return;
        }

        // 2) Block
        int b = findWinningCell(game, playerSign);
        if (b != -1) {
            game.doMove(b);
            return;
        }

        // 3) prefer center
        if (game.getSignAt(CENTER_INDEX) == Sign.EMPTY) {
            game.doMove(CENTER_INDEX);
            return;
        }

        // 4) random fallback
        List<Integer> empties = getEmptyCells(game);
        if (!empties.isEmpty()) {
            int cell = empties.get(new Random().nextInt(empties.size()));
            game.doMove(cell);
        }
    }

    private int findWinningCell(Game game, Sign sign) {
        for (int i=0; i<9; i++){
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
        for (int i=0; i<9; i++){
            if (game.getSignAt(i) == Sign.EMPTY) {
                list.add(i);
            }
        }
        return list;
    }
}