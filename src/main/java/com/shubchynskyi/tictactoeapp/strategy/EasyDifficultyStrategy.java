package com.shubchynskyi.tictactoeapp.strategy;

import com.shubchynskyi.tictactoeapp.entity.Sign;
import com.shubchynskyi.tictactoeapp.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Easy - делает вообще случайный ход из пустых клеток.
 */
public class EasyDifficultyStrategy implements DifficultyStrategy {

    @Override
    public void makeMove(Game game) {
        // Just random empty cell
        List<Integer> emptyCells = getEmptyCells(game);
        if (!emptyCells.isEmpty()) {
            int cell = emptyCells.get(new Random().nextInt(emptyCells.size()));
            game.doMove(cell); // внутренний метод в Game
        }
    }

    private List<Integer> getEmptyCells(Game game) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (game.getSignAt(i) == Sign.EMPTY) {
                result.add(i);
            }
        }
        return result;
    }
}