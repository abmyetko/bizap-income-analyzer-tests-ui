package com.capsilon.incomeanalyzer.automation.utilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public class SelenideUtilities {

    public static void clickOnPageByCoordinates(int x,int y){
        Actions actions = new Actions(getWebDriver());
        actions.moveToElement(getWebDriver().findElement(By.tagName("html")))
        .moveByOffset(x,y).click();
    }
}
