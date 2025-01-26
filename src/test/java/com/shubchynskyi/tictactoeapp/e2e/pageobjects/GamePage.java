package com.shubchynskyi.tictactoeapp.e2e.pageobjects;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class GamePage {

    private final WebDriver driver;
    @Getter
    private final BoardFragment board;
    private final WebDriverWait wait;

    private final By statusLabel = By.id("status");
    private final By scorePanel = By.id("scorePanel");
    private final By scoreHuman = By.id("scoreHuman");
    private final By scoreAI = By.id("scoreAI");
    private final By restartButton = By.id("restartBtn");

    public GamePage(WebDriver driver) {
        this.driver = driver;
        this.board = new BoardFragment(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getStatusText() {
        return driver.findElement(statusLabel).getText();
    }

    public boolean isScorePanelVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(scorePanel));
            return driver.findElement(scorePanel).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getScoreHumanText() {
        return driver.findElement(scoreHuman).getText();
    }

    public String getScoreAIText() {
        return driver.findElement(scoreAI).getText();
    }

    public void clickRestart() {
        driver.findElement(restartButton).click();
    }

    public boolean isRestartButtonVisible() {
        try {
            return driver.findElement(restartButton).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
