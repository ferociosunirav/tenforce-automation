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

            testCasesMarkup.append(buildTestCaseRow(result));
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
                    .table-wrap {
                        overflow-x: auto;
                        padding: 0;
                    }
                    .badge {
                        display: inline-block;
                        padding: 6px 10px;
                        border-radius: 999px;
                        font-size: 12px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                    }
                    .passed { background: var(--passed); }
                    .failed { background: var(--failed); }
                    .skipped { background: var(--skipped); }
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        min-width: 1100px;
                    }
                    thead {
                        background: #f2ece0;
                    }
                    th, td {
                        padding: 14px 16px;
                        border-bottom: 1px solid var(--border);
                        vertical-align: top;
                        text-align: left;
                    }
                    th {
                        font-size: 12px;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                        color: var(--muted);
                    }
                    tbody tr:nth-child(even) {
                        background: #fffcf6;
                    }
                    .test-name {
                        font-weight: 700;
                        margin-bottom: 4px;
                    }
                    .test-class {
                        color: var(--muted);
                        font-size: 13px;
                    }
                    .steps {
                        margin: 0;
                        padding-left: 18px;
                    }
                    .steps li {
                        margin: 0 0 8px;
                        line-height: 1.5;
                    }
                    .error-box {
                        border: 1px solid #e3b1af;
                        background: #fff4f3;
                        border-radius: 12px;
                        padding: 10px 12px;
                        white-space: pre-wrap;
                        font-family: Consolas, monospace;
                        font-size: 13px;
                    }
                    .empty {
                        color: var(--muted);
                        font-style: italic;
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
                    <section class="hero table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>Status</th>
                                    <th>Test Case</th>
                                    <th>Started</th>
                                    <th>Finished</th>
                                    <th>Duration</th>
                                    <th>Execution Steps</th>
                                    <th>Error</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </section>
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

    private String buildTestCaseRow(ITestResult result) {
        String statusLabel = getStatusLabel(result.getStatus());
        String statusCssClass = statusLabel.toLowerCase();

        return """
            <tr>
                <td><span class="badge %s">%s</span></td>
                <td>
                    <div class="test-name">%s</div>
                    <div class="test-class">%s</div>
                </td>
                <td>%s</td>
                <td>%s</td>
                <td>%d ms</td>
                <td>%s</td>
                <td>%s</td>
            </tr>
            """.formatted(
            statusCssClass,
            statusLabel,
            escapeHtml(result.getMethod().getMethodName()),
            escapeHtml(result.getTestClass().getName()),
            formatTime(result.getStartMillis()),
            formatTime(result.getEndMillis()),
            Math.max(result.getEndMillis() - result.getStartMillis(), 0),
            buildStepList(result),
            buildThrowableMarkup(result)
        );
    }

    @SuppressWarnings("unchecked")
    private String buildStepList(ITestResult result) {
        Object stepLogsAttribute = result.getAttribute("stepLogs");
        if (!(stepLogsAttribute instanceof List<?> stepLogs) || stepLogs.isEmpty()) {
            return "<span class=\"empty\">No step logs were captured for this test case.</span>";
        }

        StringBuilder stepsMarkup = new StringBuilder("<ol class=\"steps\">");
        for (Object step : stepLogs) {
            stepsMarkup.append("<li>").append(escapeHtml(String.valueOf(step))).append("</li>");
        }
        stepsMarkup.append("</ol>");
        return stepsMarkup.toString();
    }

    private String buildThrowableMarkup(ITestResult result) {
        if (result.getThrowable() == null) {
            return "<span class=\"empty\">None</span>";
        }

        return """
            <div class="error-box">%s</div>
            """.formatted(escapeHtml(result.getThrowable().toString()));
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
