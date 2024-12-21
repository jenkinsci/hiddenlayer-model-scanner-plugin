package com.hiddenlayer.jenkins;

import java.time.format.DateTimeFormatter;

import org.openapitools.client.model.ScanReportV3;

public class ScanReporter {

    // Generate and return a text summary for the scan report
    public static String summarizeScan(ScanReportV3 scanReport) {
        String modelName = scanReport.getInventory().getModelName();
        String modelVersion = scanReport.getInventory().getModelVersion();

        // Use a formatter to ensure seconds are always included in the printed time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        String scanEndTime = scanReport.getEndTime().format(formatter);

        StringBuilder sb = new StringBuilder(
                String.format("Scan results for model \"%s\", version %s:\n", modelName, modelVersion));
        addLine(sb, "Status", scanReport.getStatus().toString());
        addLine(sb, "Severity", scanReport.getSeverity().toString());
        addLine(sb, "End time", scanEndTime);
        addLine(sb, "Scanner version", scanReport.getVersion().toString());
        addLine(sb, "Console scan link", getScanResultsUrl(scanReport));
        return sb.toString();
    }

    private static void addLine(StringBuilder sb, String key, String value) {
        sb.append(String.format("%s: %s\n", key, value));
    }

    private static String getScanResultsUrl(ScanReportV3 scanReport) {
        return "https://console.us.hiddenlayer.ai/model-details/"
                + scanReport.getInventory().getModelId() + "/scans/" + scanReport.getScanId();
    }
}
