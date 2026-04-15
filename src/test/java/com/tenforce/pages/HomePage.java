package com.tenforce.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    private static final String BASE_URL = "https://www.tenforce.com/";

    private final By cookieButtonLocator = By.xpath(
        "//button[normalize-space()='I Agree' or normalize-space()='I agree' or contains(normalize-space(), 'Accept')]"
        + " | //a[normalize-space()='I Agree' or normalize-space()='I agree' or contains(normalize-space(), 'Accept')]"
    );

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        logStep("Navigating to URL: " + BASE_URL);
        driver.get(BASE_URL);
        logStep("URL opened successfully");
        waitForPageBody();
        return this;
    }

    public HomePage acceptCookiesIfPresent() {
        logStep("Checking for cookie consent banner");

        try {
            WebElement cookieButton = waitForVisibleElement(cookieButtonLocator);
            logStep("Cookie consent button is visible");
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", cookieButton);
            logStep("Scrolled to cookie consent button");
            highlightElement(cookieButton);
            pauseForDemo(1500);
            waitUntilClickable(cookieButton);
            clickElement(cookieButton);
            logStep("Clicked on cookie consent button");
            waitUntilInvisible(cookieButton);
            logStep("Cookie consent banner dismissed");
        } catch (Exception exception) {
            logStep("Cookie consent button not found or already dismissed: " + exception.getMessage());
        }

        return this;
    }

    public String getPageTitle() {
        logStep("Capturing page title");
        String title = driver.getTitle();
        logStep("Page title captured: " + title);
        return title;
    }
}
