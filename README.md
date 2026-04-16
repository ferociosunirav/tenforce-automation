# Tenforce

A Java project for web automation using Selenium WebDriver and TestNG.

## Prerequisites

- Java 11 or higher
- Maven
- Chrome browser

## Running Tests

mvn test

## Jenkins

This repository includes a `Jenkinsfile` for Pipeline jobs.

Expected Jenkins tools:

- JDK installation named `jdk17`
- Maven installation named `maven3`

The pipeline runs:

`mvn clean test -DHEADLESS=true`

Artifacts published by Jenkins:

- TestNG/Surefire XML results from `target/surefire-reports`
- Custom HTML automation report from `target/custom-reports/test-case-report.html`
