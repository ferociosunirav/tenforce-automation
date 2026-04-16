package com.tenforce.base;

import com.tenforce.reporting.StepLogger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod
    public void setUp(Method method) {
        StepLogger.startTestCase();
        logStep("Setting up ChromeDriver");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = buildChromeOptions();
        logStep("Opening Chrome browser");
        driver = new ChromeDriver(options);
        logStep("Chrome browser opened successfully");
        logStep("Maximizing browser window");
        driver.manage().window().maximize();
        logStep("Browser window maximized");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (driver != null) {
            logStep("Closing browser");
            driver.quit();
            logStep("Browser closed successfully");
        }

        StepLogger.finishTestCase(result);
    }

    protected void logStep(String message) {
        StepLogger.log(message);
    }

    private ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        if (isHeadlessEnabled()) {
            logStep("Headless execution is enabled for CI");
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }

        return options;
    }

    private boolean isHeadlessEnabled() {
        String headlessValue = System.getProperty("HEADLESS");
        if (headlessValue == null || headlessValue.isBlank()) {
            headlessValue = System.getenv("HEADLESS");
        }
        return headlessValue != null && headlessValue.equalsIgnoreCase("true");
    }
}
