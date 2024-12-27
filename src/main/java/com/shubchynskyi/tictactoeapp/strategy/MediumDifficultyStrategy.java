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
public class MediumDifficultyStrategy implements DifficultyStrategy {
    @Override
    public void makeMove(Game game) {
        Sign aiSign = game.getAiSign();
        Sign playerSign = game.getPlayerSign();

        // 1) AI try winning
        int winCell = findWinningCell(game, aiSign);
        if (winCell != -1) {
            game.doMove(winCell);
            return;
        }

        // 2) Block player
        int blockCell = findWinningCell(game, playerSign);
        if (blockCell != -1) {
            game.doMove(blockCell);
            return;
        }

        // 3) random fallback
        List<Integer> empties = getEmptyCells(game);
        if (!empties.isEmpty()) {
            int cell = empties.get(new Random().nextInt(empties.size()));
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