package com.tenforce.reporting;

import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CustomHtmlReportListener implements ISuiteListener {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    public void onStart(ISuite suite) {
        // No-op
    }

    @Override
    public void onFinish(ISuite suite) {
        List<ITestResult> allResults = collectResults(suite.getResults());
        allResults.sort(Comparator.comparingLong(ITestResult::getStartMillis));

        String reportContent = buildHtmlReport(suite.getName(), allResults);
        Path reportPath = Paths.get("target", "custom-reports", "test-case-report.html");

        try {
            Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, reportContent, StandardCharsets.UTF_8);
            System.out.println("[Tenforce Automation] Custom test case report generated at: " + reportPath.toAbsolutePath());
        } catch (IOException exception) {
            throw new RuntimeException("Unable to generate custom HTML report", exception);
        }
    }

    private List<ITestResult> collectResults(Map<String, ISuiteResult> suiteResults) {
        List<ITestResult> results = new ArrayList<>();

        for (ISuiteResult suiteResult : suiteResults.values()) {
            ITestContext context = suiteResult.getTestContext();
            addAllResults(results, context.getPassedTests());
            addAllResults(results, context.getFailedTests());
            addAllResults(results, context.getSkippedTests());
        }

        return results;
    }

    private void addAllResults(List<ITestResult> target, IResultMap resultMap) {
        target.addAll(resultMap.getAllResults());
    }

    private String buildHtmlReport(String suiteName, List<ITestResult> results) {
        int passedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;

        StringBuilder testCasesMarkup = new StringBuilder();

        for (ITestResult result : results) {
            if (result.getStatus() == ITestResult.SUCCESS) {
                passedCount++;
            } else if (result.getStatus() == ITestResult.FAILURE) {
                failedCount++;
            } else if (result.getStatus() == ITestResult.SKIP) {
                skippedCount++;
            }

            testCasesMarkup.append(buildTestCaseSection(result));
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Tenforce Automation Report</title>
                <style>
                    :root {
                        --bg: #f5f1e8;
                        --card: #fffdf7;
                        --ink: #1f2a30;
                        --muted: #5d6a70;
                        --border: #d7cfc0;
                        --accent: #0f766e;
                        --passed: #dff6e8;
                        --failed: #fde2e1;
                        --skipped: #fff1c9;
                    }
                    * { box-sizing: border-box; }
                    body {
                        margin: 0;
                        font-family: "Segoe UI", Tahoma, sans-serif;
                        background: linear-gradient(180deg, #f6f0e4 0%%, #efe8db 100%%);
                        color: var(--ink);
                    }
                    .wrap {
                        max-width: 1100px;
                        margin: 0 auto;
                        padding: 32px 20px 48px;
                    }
                    .hero, .card {
                        background: var(--card);
                        border: 1px solid var(--border);
                        border-radius: 18px;
                        box-shadow: 0 10px 30px rgba(31, 42, 48, 0.08);
                    }
                    .hero {
                        padding: 28px;
                        margin-bottom: 24px;
                    }
                    h1, h2, h3, p {
                        margin: 0;
                    }
                    .subtitle {
                        margin-top: 8px;
                        color: var(--muted);
                    }
                    .summary {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
                        gap: 12px;
                        margin-top: 20px;
                    }
                    .summary-card {
                        border: 1px solid var(--border);
                        border-radius: 14px;
                        padding: 16px;
                        background: #fff;
                    }
                    .summary-card strong {
                        display: block;
                        font-size: 28px;
                        margin-top: 6px;
                    }
                    .section-title {
                        margin: 28px 0 14px;
                        font-size: 22px;
                    }
                    .card {
                        padding: 22px;
                        margin-bottom: 18px;
                    }
                    .case-header {
                        display: flex;
                        justify-content: space-between;
                        gap: 16px;
                        align-items: flex-start;
                        margin-bottom: 14px;
                    }
                    .badge {
                        padding: 8px 12px;
                        border-radius: 999px;
                        font-size: 12px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                    }
                    .passed { background: var(--passed); }
                    .failed { background: var(--failed); }
                    .skipped { background: var(--skipped); }
                    .meta {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                        gap: 10px;
                        margin: 14px 0 18px;
                    }
                    .meta div {
                        border: 1px solid var(--border);
                        border-radius: 12px;
                        padding: 12px;
                        background: #fff;
                    }
                    .meta span {
                        display: block;
                        font-size: 12px;
                        color: var(--muted);
                        margin-bottom: 4px;
                        text-transform: uppercase;
                        letter-spacing: 0.06em;
                    }
                    ol {
                        margin: 0;
                        padding-left: 22px;
                    }
                    li {
                        margin: 0 0 8px;
                        line-height: 1.5;
                    }
                    .error-box {
                        margin-top: 18px;
                        border: 1px solid #e3b1af;
                        background: #fff4f3;
                        border-radius: 12px;
                        padding: 14px;
                        white-space: pre-wrap;
                        font-family: Consolas, monospace;
                        font-size: 13px;
                    }
                </style>
            </head>
            <body>
                <div class="wrap">
                    <section class="hero">
                        <h1>Tenforce Automation Report</h1>
                        <p class="subtitle">Suite: %s</p>
                        <div class="summary">
                            <div class="summary-card">
                                <span>Total Test Cases</span>
                                <strong>%d</strong>
                            </div>
                            <div class="summary-card">
                                <span>Passed</span>
                                <strong>%d</strong>
                            </div>
                            <div class="summary-card">
                                <span>Failed</span>
                                <strong>%d</strong>
                            </div>
                            <div class="summary-card">
                                <span>Skipped</span>
                                <strong>%d</strong>
                            </div>
                        </div>
                    </section>
                    <h2 class="section-title">Test Cases</h2>
                    %s
                </div>
            </body>
            </html>
            """.formatted(
            escapeHtml(suiteName),
            results.size(),
            passedCount,
            failedCount,
            skippedCount,
            testCasesMarkup
        );
    }

    private String buildTestCaseSection(ITestResult result) {
        String statusLabel = getStatusLabel(result.getStatus());
        String statusCssClass = statusLabel.toLowerCase();
        String throwableMarkup = "";

        if (result.getThrowable() != null) {
            throwableMarkup = """
                <div class="error-box">%s</div>
                """.formatted(escapeHtml(result.getThrowable().toString()));
        }

        return """
            <section class="card">
                <div class="case-header">
                    <div>
                        <h3>%s</h3>
                        <p class="subtitle">%s</p>
                    </div>
                    <span class="badge %s">%s</span>
                </div>
                <div class="meta">
                    <div>
                        <span>Started</span>
                        <strong>%s</strong>
                    </div>
                    <div>
                        <span>Finished</span>
                        <strong>%s</strong>
                    </div>
                    <div>
                        <span>Duration</span>
                        <strong>%d ms</strong>
                    </div>
                </div>
                <h3>Execution Steps</h3>
                %s
                %s
            </section>
            """.formatted(
            escapeHtml(result.getMethod().getMethodName()),
            escapeHtml(result.getTestClass().getName()),
            statusCssClass,
            statusLabel,
            formatTime(result.getStartMillis()),
            formatTime(result.getEndMillis()),
            Math.max(result.getEndMillis() - result.getStartMillis(), 0),
            buildStepList(result),
            throwableMarkup
        );
    }

    @SuppressWarnings("unchecked")
    private String buildStepList(ITestResult result) {
        Object stepLogsAttribute = result.getAttribute("stepLogs");
        if (!(stepLogsAttribute instanceof List<?> stepLogs) || stepLogs.isEmpty()) {
            return "<p class=\"subtitle\">No step logs were captured for this test case.</p>";
        }

        StringBuilder stepsMarkup = new StringBuilder("<ol>");
        for (Object step : stepLogs) {
            stepsMarkup.append("<li>").append(escapeHtml(String.valueOf(step))).append("</li>");
        }
        stepsMarkup.append("</ol>");
        return stepsMarkup.toString();
    }

    private String getStatusLabel(int status) {
        if (status == ITestResult.SUCCESS) {
            return "Passed";
        }
        if (status == ITestResult.FAILURE) {
            return "Failed";
        }
        if (status == ITestResult.SKIP) {
            return "Skipped";
        }
        return "Unknown";
    }

    private String formatTime(long epochMillis) {
        return DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    private String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
