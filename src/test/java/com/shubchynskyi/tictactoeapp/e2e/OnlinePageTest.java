package com.shubchynskyi.tictactoeapp.e2e;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.e2e.pageobjects.OnlinePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class OnlinePageTest extends BaseE2ETest {

    @Test
    @DisplayName("Check create new online game -> URL has /onlineGame?gameId=")
    void testCreateOnlineGame() {
        driver.get(getBaseUrl() + Route.ONLINE);
        OnlinePage onlinePage = new OnlinePage(driver);
        onlinePage.clickCreateGame();
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(1))
                .until(d -> Objects.requireNonNull(d.getCurrentUrl()).contains("/onlineGame?gameId="));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(Objects.requireNonNull(currentUrl).contains("/onlineGame?gameId="));
    }

    @Test
    @DisplayName("Click 'Back to mode selection' -> must navigate to /")
    void testBackToHomeLink() {
        driver.get(getBaseUrl() + Route.ONLINE);
        OnlinePage onlinePage = new OnlinePage(driver);
        onlinePage.clickBackToHome();
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(1))
                .until(d -> {
                    String url = d.getCurrentUrl();
                    return Objects.requireNonNull(url).endsWith("/") || url.endsWith("/index") || url.contains("/?");
                });
        String finalUrl = driver.getCurrentUrl();
        assertTrue(Objects.requireNonNull(finalUrl).endsWith("/") || finalUrl.endsWith("/index") || finalUrl.contains("/?"));
    }
}