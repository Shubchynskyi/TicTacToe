package com.shubchynskyi.tictactoeapp.e2e.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


public class OnlinePage {

    private final WebDriver driver;

    private final By createGameButton = By.xpath("//button[@type='submit' and contains(@class,'button is-primary')]");
    private final By backToHomeLink = By.xpath("//a[@href='/' and contains(@class,'button')]");

    public OnlinePage(WebDriver driver) {
        this.driver = driver;
    }

    public void clickCreateGame() {
        driver.findElement(createGameButton).click();
    }

    public void clickBackToHome() {
        driver.findElement(backToHomeLink).click();
    }
}