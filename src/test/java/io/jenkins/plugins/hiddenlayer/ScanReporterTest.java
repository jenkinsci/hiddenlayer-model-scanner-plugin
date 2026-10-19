package io.jenkins.plugins.hiddenlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hiddenlayer.api.models.scans.results.ScanReport;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ScanReporterTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testSummarizeScan() {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2021-01-01T00:00:00Z");

        ScanReport.Inventory inventory = ScanReport.Inventory.builder()
                .modelId("model-id")
                .modelName("perceptron")
                .modelVersionId("version-id")
                .requestedScanLocation("/path/to/model")
                .modelVersion("1.0.0")
                .build();

        ScanReport.Summary summary = ScanReport.Summary.builder()
                .detectionCategories(Collections.emptyList())
                .detectionCount(0L)
                .fileCount(1L)
                .filesFailedToScan(0L)
                .filesWithDetectionsCount(0L)
                .highestSeverity(ScanReport.Summary.HighestSeverity.NONE)
                .severity(ScanReport.Summary.Severity.SAFE)
                .unknownFiles(0L)
                .build();

        ScanReport scanReport = ScanReport.builder()
                .detectionCount(0L)
                .fileCount(1L)
                .filesWithDetectionsCount(0L)
                .inventory(inventory)
                .scanId("scan-id")
                .startTime(offsetDateTime)
                .status(ScanReport.Status.DONE)
                .summary(summary)
                .version("24.10.2")
                .endTime(offsetDateTime)
                .severity(ScanReport.Severity.SAFE)
                .build();

        String reportSummary = ScanReporter.summarizeScan(ScanResult.from(scanReport));

        assertEquals(
                "Scan results for model \"perceptron\", version 1.0.0:" + System.lineSeparator()
                        + "Status: done" + System.lineSeparator()
                        + "Severity: safe" + System.lineSeparator()
                        + "End time: 2021-01-01T00:00:00Z" + System.lineSeparator()
                        + "Scanner version: 24.10.2" + System.lineSeparator()
                        + "Console scan link: https://console.us.hiddenlayer.ai/model-details/model-id/scans/scan-id"
                        + System.lineSeparator(),
                reportSummary);
    }
}
