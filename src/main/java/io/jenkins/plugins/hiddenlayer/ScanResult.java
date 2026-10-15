package io.jenkins.plugins.hiddenlayer;

import com.hiddenlayer.api.models.scans.results.ScanReport;
import java.io.Serial;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Remoting-safe scan summary. {@link ScanReport} is an SDK type and is not safe to send over
 * Jenkins remoting, so the agent maps to this DTO before returning to the controller.
 */
public final class ScanResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter END_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public enum Severity {
        UNKNOWN,
        SAFE,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }

        static Severity fromSdk(ScanReport.Severity sdkSeverity) {
            if (sdkSeverity == null) {
                return null;
            }
            try {
                return Severity.valueOf(sdkSeverity.asString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    private final String modelName;
    private final String modelVersion;
    private final String modelId;
    private final String scanId;
    private final String status;
    private final Severity severity;
    private final String rawSeverity;
    private final String endTime;
    private final String scannerVersion;

    ScanResult(
            String modelName,
            String modelVersion,
            String modelId,
            String scanId,
            String status,
            Severity severity,
            String endTime,
            String scannerVersion) {
        this(
                modelName,
                modelVersion,
                modelId,
                scanId,
                status,
                severity,
                endTime,
                scannerVersion,
                severity == null ? null : severity.toString());
    }

    ScanResult(
            String modelName,
            String modelVersion,
            String modelId,
            String scanId,
            String status,
            Severity severity,
            String endTime,
            String scannerVersion,
            String rawSeverity) {
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.modelId = modelId;
        this.scanId = scanId;
        this.status = status;
        this.severity = severity;
        this.endTime = endTime;
        this.scannerVersion = scannerVersion;
        this.rawSeverity = rawSeverity;
    }

    public static ScanResult from(ScanReport report) {
        ScanReport.Inventory inventory = report.inventory();
        String endTime =
                report.endTime().map(dt -> dt.format(END_TIME_FORMATTER)).orElse("unknown");
        ScanReport.Severity sdkSeverity = report.severity().orElse(null);
        String rawSeverity = sdkSeverity == null ? null : sdkSeverity.asString();
        return new ScanResult(
                inventory.modelName(),
                inventory.modelVersion().orElse("unknown"),
                inventory.modelId(),
                report.scanId(),
                report.status().toString(),
                Severity.fromSdk(sdkSeverity),
                endTime,
                report.version(),
                rawSeverity);
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public String getModelId() {
        return modelId;
    }

    public String getScanId() {
        return scanId;
    }

    public String getStatus() {
        return status;
    }

    public Optional<Severity> getSeverity() {
        return Optional.ofNullable(severity);
    }

    /**
     * API severity string when it did not match a known {@link Severity}. Empty when the value was
     * null, a known level, or the SDK's own {@code unknown}.
     */
    public Optional<String> getUnrecognizedSeverity() {
        if (rawSeverity == null || rawSeverity.isBlank()) {
            return Optional.empty();
        }
        if (severity != Severity.UNKNOWN) {
            return Optional.empty();
        }
        if (Severity.UNKNOWN.toString().equalsIgnoreCase(rawSeverity)) {
            return Optional.empty();
        }
        return Optional.of(rawSeverity);
    }

    public String getEndTime() {
        return endTime;
    }

    public String getScannerVersion() {
        return scannerVersion;
    }
}
