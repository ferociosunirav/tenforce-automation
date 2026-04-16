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
        long totalDurationMillis = 0;
        int rowIndex = 1;

        for (ITestResult result : results) {
            if (result.getStatus() == ITestResult.SUCCESS) {
                passedCount++;
            } else if (result.getStatus() == ITestResult.FAILURE) {
                failedCount++;
            } else if (result.getStatus() == ITestResult.SKIP) {
                skippedCount++;
            }

            totalDurationMillis += Math.max(result.getEndMillis() - result.getStartMillis(), 0);
            testCasesMarkup.append(buildTestCaseRow(rowIndex++, result));
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
                        --bg: #f8f0de;
                        --bg-accent: #efe2c8;
                        --card: #fffdf8;
                        --ink: #10273b;
                        --muted: #586c7d;
                        --border: #d9c8aa;
                        --head: #efe5d3;
                        --passed-bg: #e2f4de;
                        --passed-ink: #15703b;
                        --failed-bg: #ffe3df;
                        --failed-ink: #c53030;
                        --skipped-bg: #fff2c7;
                        --skipped-ink: #b26a00;
                    }
                    * { box-sizing: border-box; }
                    body {
                        margin: 0;
                        font-family: "Segoe UI", Tahoma, sans-serif;
                        background:
                            radial-gradient(circle at top left, rgba(255,255,255,0.9), transparent 28%%),
                            linear-gradient(180deg, var(--bg) 0%%, var(--bg-accent) 100%%);
                        color: var(--ink);
                    }
                    .wrap {
                        max-width: 1500px;
                        margin: 0 auto;
                        padding: 36px 24px 56px;
                    }
                    .hero, .table-card {
                        background: var(--card);
                        border: 1px solid var(--border);
                        border-radius: 24px;
                        box-shadow: 0 16px 36px rgba(58, 43, 16, 0.12);
                    }
                    .hero {
                        padding: 34px 40px 30px;
                        margin-bottom: 34px;
                    }
                    h1, h2, h3, p {
                        margin: 0;
                    }
                    h1 {
                        font-size: 34px;
                        line-height: 1.15;
                    }
                    .subtitle {
                        margin-top: 10px;
                        color: var(--muted);
                        font-size: 16px;
                    }
                    .summary {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
                        gap: 18px;
                        margin-top: 28px;
                    }
                    .summary-card {
                        border: 1px solid var(--border);
                        border-radius: 18px;
                        padding: 18px 22px;
                        background: #fff;
                    }
                    .summary-card span {
                        display: block;
                        color: var(--muted);
                        font-size: 13px;
                        letter-spacing: 0.06em;
                        text-transform: uppercase;
                    }
                    .summary-card strong {
                        display: block;
                        font-size: 44px;
                        line-height: 1.1;
                        margin-top: 10px;
                    }
                    .summary-card.duration strong {
                        font-size: 34px;
                    }
                    .summary-card.passed strong { color: var(--passed-ink); }
                    .summary-card.failed strong { color: var(--failed-ink); }
                    .summary-card.skipped strong { color: var(--skipped-ink); }
                    .section-title {
                        padding: 28px 30px 20px;
                        font-size: 24px;
                        border-bottom: 1px solid var(--border);
                    }
                    .table-card {
                        overflow-x: auto;
                    }
                    .badge {
                        display: inline-block;
                        padding: 7px 14px;
                        border-radius: 999px;
                        font-size: 12px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                    }
                    .passed { background: var(--passed-bg); color: var(--passed-ink); }
                    .failed { background: var(--failed-bg); color: var(--failed-ink); }
                    .skipped { background: var(--skipped-bg); color: var(--skipped-ink); }
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                        min-width: 1350px;
                    }
                    thead {
                        background: var(--head);
                    }
                    th, td {
                        padding: 18px 16px;
                        border-bottom: 1px solid var(--border);
                        vertical-align: top;
                        text-align: left;
                    }
                    th {
                        font-size: 12px;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                        color: var(--muted);
                        white-space: nowrap;
                    }
                    th.col-index, td.index-cell { width: 56px; }
                    th.col-name, td.name-cell { width: 40%%; }
                    th.col-status, td.status-cell { width: 110px; }
                    th.col-duration, td.duration-cell { width: 100px; }
                    th.col-time, td.time-cell { width: 180px; }
                    th.col-steps, td.steps-cell { width: 160px; }
                    tbody tr {
                        background: rgba(255, 255, 255, 0.6);
                    }
                    tbody tr.status-passed {
                        box-shadow: inset 4px 0 0 #22c55e;
                    }
                    tbody tr.status-failed {
                        box-shadow: inset 4px 0 0 #ef4444;
                    }
                    tbody tr.status-skipped {
                        box-shadow: inset 4px 0 0 #f59e0b;
                    }
                    .test-name {
                        font-weight: 700;
                        margin-bottom: 6px;
                        font-size: 18px;
                    }
                    .test-description {
                        color: var(--muted);
                        font-size: 16px;
                        line-height: 1.45;
                    }
                    .index-cell,
                    .status-cell,
                    .duration-cell,
                    .time-cell,
                    .steps-cell {
                        white-space: nowrap;
                    }
                    .name-cell {
                        padding-right: 24px;
                    }
                    .steps {
                        margin: 0;
                        padding-left: 20px;
                    }
                    .steps li {
                        margin: 0 0 8px;
                        line-height: 1.5;
                    }
                    .steps-toggle {
                        cursor: pointer;
                        border: 2px solid #2d2d2d;
                        border-radius: 14px;
                        padding: 8px 14px;
                        background: #fffdfa;
                        color: #40576a;
                        user-select: none;
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        font: inherit;
                    }
                    .steps-toggle .hide-label {
                        display: none;
                    }
                    .details-row {
                        display: none;
                    }
                    .details-row.open {
                        display: table-row;
                    }
                    .details-cell {
                        padding: 24px 28px 28px 52px;
                        background: rgba(255, 253, 248, 0.95);
                    }
                    .details-title {
                        margin: 0 0 14px;
                        font-size: 14px;
                        text-transform: uppercase;
                        letter-spacing: 0.08em;
                        color: #4f6475;
                    }
                    .details-row.open + tr,
                    .details-row.open {
                        border-top: 0;
                    }
                    .error-box {
                        border: 1px solid #e3b1af;
                        background: #fff4f3;
                        border-radius: 12px;
                        padding: 10px 12px;
                        white-space: pre-wrap;
                        font-family: Consolas, monospace;
                        font-size: 13px;
                        margin-top: 12px;
                    }
                    .empty {
                        color: var(--muted);
                        font-style: italic;
                    }
                    .test-row.expanded .steps-toggle .show-label {
                        display: none;
                    }
                    .test-row.expanded .steps-toggle .hide-label {
                        display: inline;
                    }
                </style>
                <script>
                    function toggleSteps(id, button) {
                        const row = document.getElementById(id);
                        const isOpen = row.classList.toggle('open');
                        const testRow = button.closest('tr');
                        if (testRow) {
                            testRow.classList.toggle('expanded', isOpen);
                        }
                        button.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
                    }
                </script>
            </head>
            <body>
                <div class="wrap">
                    <section class="hero">
                        <h1>Tenforce Automation Report</h1>
                        <p class="subtitle">Suite: %s | Generated: %s</p>
                        <div class="summary">
                            <div class="summary-card">
                                <span>Total Test Cases</span>
                                <strong>%d</strong>
                            </div>
                            <div class="summary-card passed">
                                <span>Passed</span>
                                <strong>%d</strong>
                            </div>
                            <div class="summary-card failed">
                                <span>Failed</span>
                                <strong>%d</strong>
                            </div>
                            <div class="summary-card skipped">
                                <span>Skipped</span>
                                <strong>%d</strong>
                            </div>
                            <div class="summary-card duration">
                                <span>Total Duration</span>
                                <strong>%s</strong>
                            </div>
                        </div>
                    </section>
                    <section class="table-card">
                        <h2 class="section-title">Test Cases</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th class="col-index">#</th>
                                    <th class="col-name">Test Name / Description</th>
                                    <th class="col-status">Status</th>
                                    <th class="col-duration">Duration</th>
                                    <th class="col-time">Start Time</th>
                                    <th class="col-time">End Time</th>
                                    <th class="col-steps">Steps</th>
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
            formatTime(Instant.now().toEpochMilli()),
            results.size(),
            passedCount,
            failedCount,
            skippedCount,
            formatDuration(totalDurationMillis),
            testCasesMarkup
        );
    }

    private String buildTestCaseRow(int index, ITestResult result) {
        String statusLabel = getStatusLabel(result.getStatus());
        String statusCssClass = statusLabel.toLowerCase();
        long durationMillis = Math.max(result.getEndMillis() - result.getStartMillis(), 0);

        return """
            <tr class="test-row status-%s">
                <td class="index-cell">%d</td>
                <td class="name-cell">
                    <div class="test-name">%s</div>
                    <div class="test-description">%s</div>
                </td>
                <td class="status-cell"><span class="badge %s">%s</span></td>
                <td class="duration-cell">%s</td>
                <td class="time-cell">%s</td>
                <td class="time-cell">%s</td>
                <td class="steps-cell">
                    <button class="steps-toggle" type="button" aria-expanded="false" onclick="toggleSteps('steps-%d', this)">
                        <span class="show-label">▼ Steps</span>
                        <span class="hide-label">▲ Hide</span>
                    </button>
                </td>
            </tr>
            <tr id="steps-%d" class="details-row">
                <td colspan="7" class="details-cell">
                    <h3 class="details-title">Execution Steps</h3>
                    %s
                    %s
                </td>
            </tr>
            """.formatted(
            statusCssClass,
            index,
            escapeHtml(result.getMethod().getMethodName()),
            escapeHtml(resolveDescription(result)),
            statusCssClass,
            statusLabel,
            formatDuration(durationMillis),
            formatTime(result.getStartMillis()),
            formatTime(result.getEndMillis()),
            index,
            index,
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
            return "";
        }

        return """
            <div class="error-box">%s</div>
            """.formatted(escapeHtml(result.getThrowable().toString()));
    }

    private String resolveDescription(ITestResult result) {
        String description = result.getMethod().getDescription();
        if (description == null || description.isBlank()) {
            return result.getTestClass().getName();
        }
        return description;
    }

    private String formatDuration(long durationMillis) {
        if (durationMillis < 1000) {
            return durationMillis + " ms";
        }

        long minutes = durationMillis / 60000;
        double seconds = (durationMillis % 60000) / 1000.0;

        if (minutes > 0) {
            return "%dm %.1fs".formatted(minutes, seconds);
        }

        return "%.1f s".formatted(durationMillis / 1000.0);
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
