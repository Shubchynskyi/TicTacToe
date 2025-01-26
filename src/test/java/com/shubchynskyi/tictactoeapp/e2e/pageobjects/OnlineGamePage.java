package com.shubchynskyi.tictactoeapp.e2e.pageobjects;

import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Getter
@Setter
public class OnlineGamePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By gidLabel = By.id("gameId");
    private final By yourSymbolLabel = By.id("yourSymbol");
    private final By scoreXLabel = By.id("scoreX");
    private final By scoreOLabel = By.id("scoreO");
    private final By rematchButton = By.id("rematchBtn");
    private final By mainLeaveButton = By.xpath("//button[contains(@onclick,'showCreatorLeaveModal()')]");
    private final By confirmLeaveButton = By.xpath("//button[contains(@onclick,'confirmCreatorLeave()')]");

    public OnlineGamePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getGameIdText() {
        wait.until(ExpectedConditions.presenceOfElementLocated(gidLabel));
        return driver.findElement(gidLabel).getText().trim();
    }

    public String getYourSymbol() {
        wait.until(ExpectedConditions.presenceOfElementLocated(yourSymbolLabel));
        return driver.findElement(yourSymbolLabel).getText().trim();
    }

    public String getScoreX() {
        wait.until(ExpectedConditions.presenceOfElementLocated(scoreXLabel));
        return driver.findElement(scoreXLabel).getText().trim();
    }

    public String getScoreO() {
        wait.until(ExpectedConditions.presenceOfElementLocated(scoreOLabel));
        return driver.findElement(scoreOLabel).getText().trim();
    }

    public boolean isRematchButtonVisible() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(rematchButton));
            return driver.findElement(rematchButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickRematch() {
        wait.until(ExpectedConditions.elementToBeClickable(rematchButton));
        driver.findElement(rematchButton).click();
    }

    public void clickLeaveGameButton() {
        wait.until(ExpectedConditions.elementToBeClickable(mainLeaveButton));
        driver.findElement(mainLeaveButton).click();
    }

    public boolean isConfirmLeaveButtonVisible() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(confirmLeaveButton));
            return driver.findElement(confirmLeaveButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmCreatorLeave() {
        if (isConfirmLeaveButtonVisible()) {
            driver.findElement(confirmLeaveButton).click();
        }
    }

    public BoardFragment getBoard() {
        return new BoardFragment(driver);
    }
}
