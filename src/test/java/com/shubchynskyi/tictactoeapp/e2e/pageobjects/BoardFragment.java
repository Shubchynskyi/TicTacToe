package com.shubchynskyi.tictactoeapp.e2e.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class BoardFragment {

    private final WebDriver driver;

    public BoardFragment(WebDriver driver) {
        this.driver = driver;
    }

    private WebElement getCellElement(int index) {
        return driver.findElement(By.id("cell" + index));
    }

    public void clickCell(int row, int col) {
        int index = row * 3 + col;
        getCellElement(index).click();
    }

    public String getCellText(int row, int col) {
        int index = row * 3 + col;
        return getCellElement(index).getText();
    }
}