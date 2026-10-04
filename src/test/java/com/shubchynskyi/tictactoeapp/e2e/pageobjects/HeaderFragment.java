package com.shubchynskyi.tictactoeapp.e2e.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class HeaderFragment {

    private final WebDriverWait wait;

    private final By languageSelect = By.id("languageSelect");

    public HeaderFragment(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void switchToEnglish() {
        switchLanguage("en");
    }

    public void switchToGerman() {
        switchLanguage("de");
    }

    public void switchToUkrainian() {
        switchLanguage("ua");
    }

    public void switchToRussian() {
        switchLanguage("ru");
    }

    private void switchLanguage(String language) {
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(languageSelect));
        Select select = new Select(dropdown);
        boolean changesLanguage = !language.equals(select.getFirstSelectedOption().getDomAttribute("value"));
        select.selectByValue(language);
        if (changesLanguage) {
            wait.until(ExpectedConditions.stalenessOf(dropdown));
            wait.until(ExpectedConditions.elementToBeClickable(languageSelect));
        }
    }
}
