package com.tenforce;

import com.tenforce.base.BaseTest;
import com.tenforce.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TenforceWebAutomationTest extends BaseTest {

    @Test(description = "Validate that the TenForce home page opens successfully and accepts cookies")
    public void testOpenHomePageAndAcceptCookies() {
        HomePage homePage = new HomePage(driver);
        homePage.open()
            .acceptCookiesIfPresent();

        String title = homePage.getPageTitle();
        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertFalse(title.isBlank(), "Page title should not be blank");
        logStep("Home page title validation completed");
    }
}
