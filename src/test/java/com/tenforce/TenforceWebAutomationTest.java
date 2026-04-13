package com.tenforce;

import com.tenforce.reporting.StepLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.lang.reflect.Method;

public class TenforceWebAutomationTest {

    private static final String BASE_URL = "https://www.tenforce.com/";
    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(15);
    private WebDriver driver;

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

    @Test(description = "Validate that the TenForce home page opens successfully")
    public void testHomePageLoadsSuccessfully() {
        openHomePage();

        logStep("Capturing page title");
        String title = driver.getTitle();
        logStep("Page title captured: " + title);
        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertFalse(title.isBlank(), "Page title should not be blank");
        logStep("Home page title validation completed");
    }

    @Test(description = "Validate that the home page title contains the TenForce brand")
    public void testHomePageTitleContainsTenForce() {
        openHomePage();

        logStep("Validating that the page title contains the TenForce brand name");
        String title = driver.getTitle();
        Assert.assertTrue(title.toLowerCase().contains("tenforce"), "Page title should contain TenForce");
        logStep("TenForce branding is present in the page title");
    }

    @Test(description = "Validate that the TenForce URL uses HTTPS and the company domain")
    public void testHomePageUrlIsCorrect() {
        openHomePage();

        logStep("Capturing current browser URL");
        String currentUrl = driver.getCurrentUrl();
        logStep("Current URL captured: " + currentUrl);
        Assert.assertTrue(currentUrl.startsWith("https://www.tenforce.com"), "Current URL should use the TenForce HTTPS domain");
        logStep("Current URL validation completed");
    }

    @Test(description = "Validate that the main page body is visible after the site loads")
    public void testMainPageBodyIsVisible() {
        openHomePage();

        logStep("Checking whether the page body is visible");
        WebElement body = waitForVisibleElement(By.tagName("body"));
        Assert.assertTrue(body.isDisplayed(), "Page body should be visible");
        logStep("Page body visibility validation completed");
    }

    @Test(description = "Validate that the home page contains multiple navigation links")
    public void testHomePageContainsNavigationLinks() {
        openHomePage();

        logStep("Collecting visible navigation links from the home page");
        int visibleLinks = driver.findElements(By.xpath("//a[normalize-space()]")).size();
        logStep("Visible link count captured: " + visibleLinks);
        Assert.assertTrue(visibleLinks >= 5, "Home page should contain at least 5 visible links");
        logStep("Navigation links validation completed");
    }

    private void openHomePage() {
        logStep("Navigating to URL: " + BASE_URL);
        driver.get(BASE_URL);
        logStep("URL opened successfully");
        waitForVisibleElement(By.tagName("body"));
        logStep("Home page body is visible");
        acceptCookiesIfPresent();
    }

    private void acceptCookiesIfPresent() {
        logStep("Checking for cookie consent banner");
        By cookieButtonLocator = By.xpath(
            "//button[normalize-space()='I Agree' or normalize-space()='I agree' or contains(normalize-space(), 'Accept')]"
            + " | //a[normalize-space()='I Agree' or normalize-space()='I agree' or contains(normalize-space(), 'Accept')]"
        );

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
        } catch (Exception e) {
            logStep("Cookie consent button not found or already dismissed: " + e.getMessage());
        }
    }

    private void clickElement(WebElement element) {
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

    private void highlightElement(WebElement element) {
        logStep("Highlighting cookie consent button before click");
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].style.border='3px solid red'; arguments[0].style.backgroundColor='yellow';",
            element
        );
    }

    private void pauseForDemo(long milliseconds) {
        try {
            logStep("Pausing briefly so the click action is visible");
            Thread.sleep(milliseconds);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            logStep("Pause interrupted: " + interruptedException.getMessage());
        }
    }

    private void logStep(String message) {
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
        String headlessValue = System.getenv("HEADLESS");
        return headlessValue != null && headlessValue.equalsIgnoreCase("true");
    }

    private WebElement waitForVisibleElement(By locator) {
        return new WebDriverWait(driver, DEFAULT_WAIT)
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitUntilClickable(WebElement element) {
        new WebDriverWait(driver, DEFAULT_WAIT)
            .until(ExpectedConditions.elementToBeClickable(element));
    }

    private void waitUntilInvisible(WebElement element) {
        new WebDriverWait(driver, DEFAULT_WAIT)
            .until(ExpectedConditions.invisibilityOf(element));
    }
}
