package com.shubchynskyi.tictactoeapp.e2e;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.e2e.pageobjects.GamePage;
import com.shubchynskyi.tictactoeapp.e2e.pageobjects.IndexPage;
import com.shubchynskyi.tictactoeapp.enums.Difficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LocalGameFullTest extends BaseE2ETest {

    @Test
    @DisplayName("Single-player (IMPOSSIBLE): AI wins, check score, then Restart")
    void testSinglePlayerImpossibleAiVictory() {
        driver.get(getBaseUrl() + Route.INDEX);
        IndexPage index = new IndexPage(driver);
        index.selectSingleMode();
        index.chooseSymbolX();
        index.selectDifficulty(Difficulty.IMPOSSIBLE.getValue());
        index.clickNewGame();

        GamePage game = new GamePage(driver);

        game.getBoard().clickCell(0, 0);
        game.getBoard().clickCell(0, 1);
        game.getBoard().clickCell(1, 0);

        String scoreHuman = game.getScoreHumanText();
        String scoreAI = game.getScoreAIText();
        int humanPoints = Integer.parseInt(scoreHuman);
        int aiPoints = Integer.parseInt(scoreAI);

        assertEquals(0, humanPoints, "Human score should be 0");
        assertEquals(1, aiPoints, "AI score should be 1 after AI wins");

        game.clickRestart();
        waitForBoardClear(game);

        int occupied = countOccupiedCells(game);
        assertEquals(0, occupied, "All cells should be empty after restart");
    }

    @Test
    @DisplayName("Local two-players: finish the game until X or O wins, then Restart")
    void testLocalTwoPlayersVictory() {
        driver.get(getBaseUrl() + Route.INDEX);
        IndexPage index = new IndexPage(driver);
        index.selectLocalMode();
        index.clickNewGame();

        GamePage game = new GamePage(driver);

        game.getBoard().clickCell(0, 0);
        waitForOccupiedCells(game, 1);

        game.getBoard().clickCell(1, 1);
        waitForOccupiedCells(game, 2);

        game.getBoard().clickCell(0, 1);
        waitForOccupiedCells(game, 3);

        game.getBoard().clickCell(2, 2);
        waitForOccupiedCells(game, 4);

        game.getBoard().clickCell(0, 2);

        waitForRestartButtonVisible(game);

        game.clickRestart();
        waitForBoardClear(game);

        int occupied = countOccupiedCells(game);
        assertEquals(0, occupied, "Board should be empty after restart");
    }

    private void waitForOccupiedCells(GamePage game, int expected) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(driver1 -> countOccupiedCells(game) >= expected);
        } catch (TimeoutException e) {
            fail("Expected at least " + expected + " cells to be occupied, but they were not.");
        }
    }

    private void waitForBoardClear(GamePage game) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(driver1 -> countOccupiedCells(game) == 0);
        } catch (TimeoutException e) {
            fail("Board was not cleared after restart.");
        }
    }

    private void waitForRestartButtonVisible(GamePage game) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(driver1 -> game.isRestartButtonVisible());
        } catch (TimeoutException e) {
            fail("Restart button did not appear after the game was won.");
        }
    }

    private int countOccupiedCells(GamePage game) {
        int occupied = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String text = game.getBoard().getCellText(row, col).trim();
                if (!text.isEmpty()) {
                    occupied++;
                }
            }
        }
        return occupied;
    }
}