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

public class GamePageTest extends BaseE2ETest {

    @Test
    @DisplayName("Verify status and score panel in single-player mode")
    void testSingleGameStatusAndScore() {
        driver.get(getBaseUrl() + Route.INDEX);
        IndexPage indexPage = new IndexPage(driver);
        indexPage.selectSingleMode();
        indexPage.selectDifficulty(Difficulty.IMPOSSIBLE.getValue());
        indexPage.chooseSymbolO();
        indexPage.clickNewGame();

        GamePage gamePage = new GamePage(driver);

        assertTrue(gamePage.isScorePanelVisible(), "Score panel should be visible");

        gamePage.getBoard().clickCell(0, 1);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(driver1 -> {
                int occupiedCells = 0;
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        String cellText = gamePage.getBoard().getCellText(row, col).trim();
                        if (!cellText.isEmpty()) {
                            occupiedCells++;
                        }
                    }
                }
                return occupiedCells == 3;
            });
        } catch (TimeoutException e) {
            fail("Two cells were not occupied after making a move");
        }

        String cell00 = gamePage.getBoard().getCellText(0, 1).trim();
        assertFalse(cell00.isEmpty(), "Cell (0,0) should be occupied by the player");

        boolean secondCellOccupied = false;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (row == 0 && col == 0) continue;
                String cellText = gamePage.getBoard().getCellText(row, col).trim();
                if (!cellText.isEmpty()) {
                    secondCellOccupied = true;
                    break;
                }
            }
            if (secondCellOccupied) break;
        }
        assertTrue(secondCellOccupied, "There should be at least one additional occupied cell after the player's move");

        String status = gamePage.getStatusText();
        assertNotNull(status, "Status should not be null");
        assertFalse(status.trim().isEmpty(), "Status should not be empty");
    }

    @Test
    @DisplayName("Verify Restart button in local mode")
    void testRestartLocalMode() {
        driver.get(getBaseUrl() + Route.INDEX);
        IndexPage indexPage = new IndexPage(driver);
        indexPage.selectLocalMode();
        indexPage.clickNewGame();

        GamePage gamePage = new GamePage(driver);

        gamePage.getBoard().clickCell(0, 0);
        gamePage.getBoard().clickCell(0, 1);

        gamePage.clickRestart();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(driver1 ->
                    gamePage.getBoard().getCellText(0, 0).trim().isEmpty() &&
                            gamePage.getBoard().getCellText(0, 1).trim().isEmpty()
            );
        } catch (TimeoutException e) {
            fail("Cells (0,0) and (0,1) were not cleared after restart");
        }

        String cell00 = gamePage.getBoard().getCellText(0, 0).trim();
        String cell01 = gamePage.getBoard().getCellText(0, 1).trim();
        assertEquals("", cell00, "Cell (0,0) should be empty after restart");
        assertEquals("", cell01, "Cell (0,1) should be empty after restart");
    }
}