package com.tenforce.pages;

import com.tenforce.reporting.StepLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(15);

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, DEFAULT_WAIT);
    }

    protected void logStep(String message) {
        StepLogger.log(message);
    }

    protected WebElement waitForVisibleElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitUntilClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void waitUntilInvisible(WebElement element) {
        wait.until(ExpectedConditions.invisibilityOf(element));
    }

    protected void waitForPageBody() {
        try {
            waitForVisibleElement(By.tagName("body"));
            logStep("Home page body is visible");
        } catch (NoSuchWindowException exception) {
            throw exception;
        } catch (Exception exception) {
            logStep("Unable to confirm body visibility, checking page readiness instead");
            wait.until(webDriver ->
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
            );
            logStep("Page load completed based on document readiness");
        }
    }

    protected void clickElement(WebElement element) {
        try {
            logStep("Attempting standard click");
            element.click();
            logStep("Standard click completed");
        } catch (Exception clickException) {
            logStep("Standard click failed, attempting JavaScript click");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            logStep("JavaScript click completed");
        }
    }

    protected void highlightElement(WebElement element) {
        logStep("Highlighting cookie consent button before click");
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].style.border='3px solid red'; arguments[0].style.backgroundColor='yellow';",
            element
        );
    }

    protected void pauseForDemo(long milliseconds) {
        try {
            logStep("Pausing briefly so the click action is visible");
            Thread.sleep(milliseconds);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            logStep("Pause interrupted: " + interruptedException.getMessage());
        }
    }
}
