package com.tenforce.reporting;

import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.List;

public final class StepLogger {

    private static final ThreadLocal<List<String>> CURRENT_TEST_STEPS = new ThreadLocal<>();

    private StepLogger() {
    }

    public static void startTestCase() {
        CURRENT_TEST_STEPS.set(new ArrayList<>());
    }

    public static void log(String message) {
        String logMessage = "[Tenforce Automation] " + message;
        List<String> steps = CURRENT_TEST_STEPS.get();
        if (steps != null) {
            steps.add(logMessage);
        }
        System.out.println(logMessage);
    }

    public static void finishTestCase(ITestResult result) {
        List<String> steps = CURRENT_TEST_STEPS.get();
        if (result != null && steps != null) {
            result.setAttribute("stepLogs", new ArrayList<>(steps));
        }
        CURRENT_TEST_STEPS.remove();
    }
}
