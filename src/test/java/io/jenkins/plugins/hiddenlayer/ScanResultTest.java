package io.jenkins.plugins.hiddenlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hiddenlayer.api.models.scans.results.ScanReport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ScanResultTest {

    @Test
    void fromMapsSdkReportFields() {
        OffsetDateTime endTime = OffsetDateTime.parse("2021-01-01T00:00:00Z");
        ScanReport scanReport = ScanReport.builder()
                .detectionCount(0L)
                .fileCount(1L)
                .filesWithDetectionsCount(0L)
                .inventory(ScanReport.Inventory.builder()
                        .modelId("model-id")
                        .modelName("perceptron")
                        .modelVersionId("version-id")
                        .requestedScanLocation("/path/to/model")
                        .modelVersion("1.0.0")
                        .build())
                .scanId("scan-id")
                .startTime(endTime)
                .status(ScanReport.Status.DONE)
                .summary(ScanReport.Summary.builder()
                        .detectionCategories(Collections.emptyList())
                        .detectionCount(0L)
                        .fileCount(1L)
                        .filesFailedToScan(0L)
                        .filesWithDetectionsCount(0L)
                        .highestSeverity(ScanReport.Summary.HighestSeverity.NONE)
                        .severity(ScanReport.Summary.Severity.SAFE)
                        .unknownFiles(0L)
                        .build())
                .version("24.10.2")
                .endTime(endTime)
                .severity(ScanReport.Severity.SAFE)
                .build();

        ScanResult result = ScanResult.from(scanReport);

        assertEquals("perceptron", result.getModelName());
        assertEquals("1.0.0", result.getModelVersion());
        assertEquals("model-id", result.getModelId());
        assertEquals("scan-id", result.getScanId());
        assertEquals("done", result.getStatus());
        assertEquals(ScanResult.Severity.SAFE, result.getSeverity().orElse(null));
        assertEquals("2021-01-01T00:00:00Z", result.getEndTime());
        assertEquals("24.10.2", result.getScannerVersion());
        assertTrue(result.getUnrecognizedSeverity().isEmpty());
    }

    @Test
    void roundTripsThroughJavaSerialization() throws Exception {
        ScanResult original = new ScanResult(
                "perceptron",
                "1.0.0",
                "model-id",
                "scan-id",
                "done",
                ScanResult.Severity.SAFE,
                "2021-01-01T00:00:00Z",
                "24.10.2");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }

        ScanResult restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (ScanResult) in.readObject();
        }

        assertEquals(original.getModelName(), restored.getModelName());
        assertEquals(original.getSeverity(), restored.getSeverity());
        assertEquals(original.getScanId(), restored.getScanId());
    }

    @Test
    void fromPreservesUnrecognizedSdkSeverity() {
        OffsetDateTime endTime = OffsetDateTime.parse("2021-01-01T00:00:00Z");
        ScanReport scanReport = ScanReport.builder()
                .detectionCount(0L)
                .fileCount(1L)
                .filesWithDetectionsCount(0L)
                .inventory(ScanReport.Inventory.builder()
                        .modelId("model-id")
                        .modelName("perceptron")
                        .modelVersionId("version-id")
                        .requestedScanLocation("/path/to/model")
                        .modelVersion("1.0.0")
                        .build())
                .scanId("scan-id")
                .startTime(endTime)
                .status(ScanReport.Status.DONE)
                .summary(ScanReport.Summary.builder()
                        .detectionCategories(Collections.emptyList())
                        .detectionCount(0L)
                        .fileCount(1L)
                        .filesFailedToScan(0L)
                        .filesWithDetectionsCount(0L)
                        .highestSeverity(ScanReport.Summary.HighestSeverity.NONE)
                        .severity(ScanReport.Summary.Severity.SAFE)
                        .unknownFiles(0L)
                        .build())
                .version("24.10.2")
                .endTime(endTime)
                .severity(ScanReport.Severity.of("apocalyptic"))
                .build();

        ScanResult result = ScanResult.from(scanReport);

        assertEquals(ScanResult.Severity.UNKNOWN, result.getSeverity().orElse(null));
        assertEquals("apocalyptic", result.getUnrecognizedSeverity().orElse(null));
    }
}
