package com.shubchynskyi.tictactoeapp.e2e;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.e2e.pageobjects.OnlineGamePage;
import com.shubchynskyi.tictactoeapp.e2e.pageobjects.OnlinePage;
import com.shubchynskyi.tictactoeapp.enums.Sign;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnlineGameFullTest extends BaseE2ETest {

    private WebDriver driver1;
    private WebDriver driver2;

    @BeforeEach
    void setUpDrivers() {
        driver1 = this.driver;
        driver2 = new ChromeDriver(getChromeOptions());
    }

    @AfterEach
    void tearDownDrivers() {
        if (driver2 != null) {
            driver2.quit();
        }
    }

    @Test
    @DisplayName("User1 wins top row, score increments, rematch, creator leaves, user2 kicked with possible session rewriting")
    void testOnlineGameFull() {
        driver1.get(getBaseUrl() + Route.ONLINE);
        OnlinePage page1 = new OnlinePage(driver1);
        page1.clickCreateGame();
        OnlineGamePage user1Game = new OnlineGamePage(driver1);
        new WebDriverWait(driver1, Duration.ofSeconds(5))
                .until(d -> {
                    String u = d.getCurrentUrl();
                    return Objects.requireNonNull(u).contains(Route.ONLINE_GAME) && u.contains("gameId=");
                });
        String gameId = user1Game.getGameIdText();

        driver2.get(getBaseUrl() + "/join-online?gameId=" + gameId);
        OnlineGamePage user2Game = new OnlineGamePage(driver2);
        new WebDriverWait(driver2, Duration.ofSeconds(5))
                .until(d -> {
                    String u = d.getCurrentUrl();
                    return Objects.requireNonNull(u).contains(Route.ONLINE_GAME) && u.contains("gameId=");
                });
        if (user1Game.getYourSymbol().equals(Sign.CROSS.getSign())) {
            user1Game.getBoard().clickCell(0, 0);
            user2Game.getBoard().clickCell(0, 2);
            user1Game.getBoard().clickCell(1, 1);
            user2Game.getBoard().clickCell(2, 0);
            user1Game.getBoard().clickCell(2, 2);
        } else {
            user2Game.getBoard().clickCell(0, 0);
            user1Game.getBoard().clickCell(0, 2);
            user2Game.getBoard().clickCell(1, 1);
            user1Game.getBoard().clickCell(2, 0);
            user2Game.getBoard().clickCell(2, 2);
        }

        new WebDriverWait(driver1, Duration.ofSeconds(5))
                .until(d -> {
                    int x = Integer.parseInt(user1Game.getScoreX());
                    int o = Integer.parseInt(user1Game.getScoreO());
                    return x == 1 || o == 1;
                });
        int scoreX = Integer.parseInt(user1Game.getScoreX());
        int scoreO = Integer.parseInt(user1Game.getScoreO());
        assertTrue(scoreX == 1 || scoreO == 1);

        assertTrue(user1Game.isRematchButtonVisible());
        assertTrue(user2Game.isRematchButtonVisible());
        user1Game.clickRematch();
        new WebDriverWait(driver1, Duration.ofSeconds(5))
                .until(d -> countOccupiedCells(user1Game) == 0 && countOccupiedCells(user2Game) == 0);
        assertEquals(0, countOccupiedCells(user1Game));
        assertEquals(0, countOccupiedCells(user2Game));

        user1Game.clickLeaveGameButton();
        new WebDriverWait(driver1, Duration.ofSeconds(2))
                .until(d -> user1Game.isConfirmLeaveButtonVisible());
        user1Game.confirmCreatorLeave();
        new WebDriverWait(driver2, Duration.ofSeconds(8))
                .until(d -> {
                    String u2 = d.getCurrentUrl();
                    return Objects.requireNonNull(u2).contains(Route.ONLINE) || u2.contains("CLOSED");
                });
        String finalUrl = driver2.getCurrentUrl();
        assertTrue(Objects.requireNonNull(finalUrl).contains(Route.ONLINE) || finalUrl.contains("CLOSED"));
    }

    private int countOccupiedCells(OnlineGamePage page) {
        int occupied = 0;
        for (int i = 0; i < 9; i++) {
            String t = page.getBoard().getCellText(i / 3, i % 3);
            if (!t.isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }
}