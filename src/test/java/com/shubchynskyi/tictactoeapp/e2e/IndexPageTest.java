package com.shubchynskyi.tictactoeapp.e2e;

import com.shubchynskyi.tictactoeapp.constants.Route;
import com.shubchynskyi.tictactoeapp.constants.View;
import com.shubchynskyi.tictactoeapp.e2e.pageobjects.IndexPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

import static com.shubchynskyi.tictactoeapp.TestsConstant.FIRST_USER_NAME;
import static org.junit.jupiter.api.Assertions.*;

public class IndexPageTest extends BaseE2ETest {

    @Test
    @DisplayName("Change nickname, select single mode, click New Game -> URL has /game")
    void testChangeNickAndSelectSingle() {
        driver.get(getBaseUrl() + Route.INDEX);
        IndexPage indexPage = new IndexPage(driver);

        indexPage.setNickname(FIRST_USER_NAME);
        indexPage.selectSingleMode();

        assertEquals(indexPage.getCurrentNick(), FIRST_USER_NAME);

        indexPage.clickNewGame();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(Objects.requireNonNull(currentUrl).contains(View.GAME), "Expect to go to /game");
    }

    @Test
    @DisplayName("Select local mode -> New Game -> URL has /game")
    void testSelectLocalMode() {
        driver.get(getBaseUrl() + Route.INDEX);
        IndexPage indexPage = new IndexPage(driver);

        indexPage.selectLocalMode();
        indexPage.clickNewGame();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(Objects.requireNonNull(currentUrl).contains(View.GAME), "Expect to go to /game");
    }

    @Test
    @DisplayName("Switch EN -> DE -> RU -> UA, checking nickname label text each time")
    void testSwitchAllLanguages() {
        driver.get(getBaseUrl() + Route.INDEX);
        IndexPage indexPage = new IndexPage(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        indexPage.getHeader().switchToEnglish();
        try {
            wait.until(driver1 -> "Your nickname:".equals(indexPage.getNicknameLabelText()));
        } catch (TimeoutException e) {
            fail("Nickname label did not change to English");
        }
        String labelEn = indexPage.getNicknameLabelText();
        assertEquals("Your nickname:", labelEn, "Expected 'Your nickname:' in English");

        indexPage.getHeader().switchToGerman();
        try {
            wait.until(driver1 -> "Ihr Spitzname:".equals(indexPage.getNicknameLabelText()));
        } catch (TimeoutException e) {
            fail("Nickname label did not change to German");
        }
        String labelDe = indexPage.getNicknameLabelText();
        assertEquals("Ihr Spitzname:", labelDe, "Expected 'Ihr Spitzname:' in German");

        indexPage.getHeader().switchToRussian();
        try {
            wait.until(driver1 -> "Ваш ник:".equals(indexPage.getNicknameLabelText()));
        } catch (TimeoutException e) {
            fail("Nickname label did not change to Russian");
        }
        String labelRu = indexPage.getNicknameLabelText();
        assertEquals("Ваш ник:", labelRu, "Expected 'Ваш ник:' in Russian");

        indexPage.getHeader().switchToUkrainian();
        try {
            wait.until(driver1 -> "Ваш нік:".equals(indexPage.getNicknameLabelText()));
        } catch (TimeoutException e) {
            fail("Nickname label did not change to Ukrainian");
        }
        String labelUa = indexPage.getNicknameLabelText();
        assertEquals("Ваш нік:", labelUa, "Expected 'Ваш нік:' in Ukrainian");
    }
}
