# TenForce Automation Assessment

This repository contains the automated UI test for the TenForce assessment scenario using Java, Selenium WebDriver, TestNG, and Maven.

## Scenario Covered

The automated test covers this flow:

1. Open `https://www.tenforce.com/`
2. Navigate to the Career page
3. Open the Life at TenForce section
4. Open the `Life of two interns` article
5. Scroll through the article
6. Return to Job Openings and validate the expected content

## How To Run

### Prerequisites

- Java 17
- Maven 3.x
- Google Chrome installed

### Run from terminal

From the project root, run:

```bash
mvn test
```

### Run in headless mode

This is useful for CI or Jenkins:

```bash
mvn clean test -DHEADLESS=true
```

### Reports

After execution, reports are available in:

- `target/surefire-reports/`
- `target/custom-reports/test-case-report.html`

## Framework Choice

I used Selenium WebDriver with Java because it is a standard and reliable choice for browser automation, with strong ecosystem support and good maintainability for UI test projects. TestNG was selected for test structure and assertions, while Maven keeps dependency management and command-line execution simple. WebDriverManager was added to avoid manual driver setup and make local and CI execution smoother.

## Architecture

The project follows the Page Object Model pattern. Test flow stays in the test class, while page-specific navigation and browser actions are encapsulated inside page classes, which keeps the tests easier to read and maintain. A shared base test handles WebDriver setup and teardown, and a shared base page centralizes common browser utilities such as waits, scrolling, and reusable interactions.

This separation helps keep locators and page behavior in one place, supports future test growth, and makes the suite easier to run both locally and in Jenkins.

## CI

The repository includes a `Jenkinsfile` that runs the test suite and archives reports for CI execution.
