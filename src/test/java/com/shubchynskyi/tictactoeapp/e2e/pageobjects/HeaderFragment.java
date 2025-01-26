package com.shubchynskyi.tictactoeapp.e2e.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class HeaderFragment {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By englishLink = By.xpath("//a[@onclick=\"changeLanguage('en');\"]");
    private final By germanLink = By.xpath("//a[@onclick=\"changeLanguage('de');\"]");
    private final By ukrainianLink = By.xpath("//a[@onclick=\"changeLanguage('ua');\"]");
    private final By russianLink = By.xpath("//a[@onclick=\"changeLanguage('ru');\"]");

    public HeaderFragment(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void switchToEnglish() {
        wait.until(ExpectedConditions.elementToBeClickable(englishLink));
        driver.findElement(englishLink).click();
    }

    public void switchToGerman() {
        wait.until(ExpectedConditions.elementToBeClickable(germanLink));
        driver.findElement(germanLink).click();
    }

    public void switchToUkrainian() {
        wait.until(ExpectedConditions.elementToBeClickable(ukrainianLink));
        driver.findElement(ukrainianLink).click();
    }

    public void switchToRussian() {
        wait.until(ExpectedConditions.elementToBeClickable(russianLink));
        driver.findElement(russianLink).click();
    }
}
