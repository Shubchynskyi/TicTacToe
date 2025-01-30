package com.shubchynskyi.tictactoeapp.e2e.pageobjects;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class IndexPage {
    private final WebDriver driver;
    @Getter
    private final HeaderFragment header;
    private final WebDriverWait wait;

    // Селекторы
    private final By nicknameInput = By.id("nickInput");
    private final By currentNick = By.id("nick");
    private final By changeNickButton = By.xpath("//button[contains(@onclick,'changeNickname()')]");
    private final By singleModeButton = By.id("btnSingle");
    private final By localModeButton = By.id("btnLocal");
    private final By symbolXButton = By.id("btnX");
    private final By symbolOButton = By.id("btnO");
    private final By nextDifficultyButton = By.xpath("//button[contains(@onclick,'nextDifficulty()')]");
    private final By difficultyDisplay = By.id("diffDisplay");
    private final By newGameButton = By.xpath("//button[@type='submit' and @class='button is-primary btn-w200 mt-1']");

    public IndexPage(WebDriver driver) {
        this.driver = driver;
        this.header = new HeaderFragment(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    }

    // Методы взаимодействия
    public void setNickname(String newNick) {
        driver.findElement(nicknameInput).clear();
        driver.findElement(nicknameInput).sendKeys(newNick);
        driver.findElement(changeNickButton).click();
    }

    public void selectSingleMode() {
        wait.until(ExpectedConditions.elementToBeClickable(singleModeButton)).click();
    }

    public void selectLocalMode() {
        wait.until(ExpectedConditions.elementToBeClickable(localModeButton)).click();
    }

    public void chooseSymbolX() {
        driver.findElement(symbolXButton).click();
    }

    public void chooseSymbolO() {
        driver.findElement(symbolOButton).click();
    }

    public void selectDifficulty(String targetDifficulty) {
        while (true) {
            String currentDifficulty = driver.findElement(difficultyDisplay).getText();
            if (currentDifficulty.equals(targetDifficulty)) {
                break; // Выходим из цикла, если сложность совпадает
            }
            driver.findElement(nextDifficultyButton).click(); // Кликаем на кнопку "следующая сложность"
        }
    }

    public void clickNewGame() {
        driver.findElement(newGameButton).click();
    }

    public String getNicknameLabelText() {
        String text = driver.findElement(By.xpath("//label[@class='nickname']")).getText();
        return text.trim();
    }

    public String getCurrentNick() {
        return driver.findElement(currentNick).getText();
    }
}