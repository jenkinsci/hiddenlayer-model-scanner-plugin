package io.jenkins.plugins.hiddenlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hiddenlayer.sdk.rest.models.ModelInventoryInfo;
import com.hiddenlayer.sdk.rest.models.ScanReportV3;
import java.time.OffsetDateTime;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class ScanReporterTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testSummarizeScan() {

        ModelInventoryInfo mii = new ModelInventoryInfo();
        mii.setModelName("perceptron");
        mii.setModelVersion("1.0.0");
        mii.setModelId("model-id");

        ScanReportV3 scanReport = new ScanReportV3();
        scanReport.setStatus(ScanReportV3.StatusEnum.DONE);
        scanReport.setInventory(mii);
        scanReport.setScanId("scan-id");
        scanReport.setSeverity(ScanReportV3.SeverityEnum.SAFE);
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2021-01-01T00:00:00Z");
        scanReport.setEndTime(offsetDateTime);
        scanReport.setVersion("24.10.2");
        String summary = ScanReporter.summarizeScan(scanReport);

        assertEquals(
                "Scan results for model \"perceptron\", version 1.0.0:\n" + "Status: done\n"
                        + "Severity: safe\n"
                        + "End time: 2021-01-01T00:00:00Z\n"
                        + "Scanner version: 24.10.2\n"
                        + "Console scan link: https://console.us.hiddenlayer.ai/model-details/model-id/scans/scan-id\n",
                summary);
    }
}
