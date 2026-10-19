package io.jenkins.plugins.hiddenlayer;

public class ScanReporter {

    public static String summarizeScan(ScanResult scanResult) {
        StringBuilder sb = new StringBuilder(String.format(
                "Scan results for model \"%s\", version %s:%n",
                scanResult.getModelName(), scanResult.getModelVersion()));
        addLine(sb, "Status", scanResult.getStatus());
        addLine(sb, "Severity", scanResult.getSeverity().map(Object::toString).orElse("unknown"));
        addLine(sb, "End time", scanResult.getEndTime());
        addLine(sb, "Scanner version", scanResult.getScannerVersion());
        addLine(sb, "Console scan link", getScanResultsUrl(scanResult));
        return sb.toString();
    }

    private static void addLine(StringBuilder sb, String key, String value) {
        sb.append(String.format("%s: %s%n", key, value));
    }

    private static String getScanResultsUrl(ScanResult scanResult) {
        return "https://console.us.hiddenlayer.ai/model-details/" + scanResult.getModelId() + "/scans/"
                + scanResult.getScanId();
    }
}
