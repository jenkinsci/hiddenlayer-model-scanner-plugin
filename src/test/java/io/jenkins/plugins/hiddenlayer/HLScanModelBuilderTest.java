package io.jenkins.plugins.hiddenlayer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hiddenlayer.api.models.scans.results.ScanReport;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.model.Slave;
import hudson.util.Secret;
import java.io.IOException;
import java.io.Serial;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class HLScanModelBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    // Builder parameters
    final String modelName = "model-name";
    final String hlClientId = "client-id";
    final String hlClientSecret = "client-secret";
    final String folderToScan = "folder-to-scan";
    final boolean failUnsupported = true;
    final FailOnDetectionSeverityEnum failSeverity = FailOnDetectionSeverityEnum.NONE;

    // This text is printed to the console when the plugin is run.
    final String scanMessage = String.format("Scanning model %s in folder %s", modelName, folderToScan);

    // Test objects
    private ScannerService mockScannerService;
    private String modelVersion = "1.0.0";
    private String modelId = "model-id";
    private String scanId = "scan-id";

    @Before
    public void setUp() throws IOException, URISyntaxException, InterruptedException, Exception {
        mockScannerService = mock(ScannerService.class);

        ScanReport scanReport = createScanReport(modelName, modelId, modelVersion, scanId);

        when(mockScannerService.scanFolder(eq(modelName), anyString())).thenReturn(scanReport);
        ModelScanServiceFactory.setTestInstance(mockScannerService);
    }

    @After
    public void tearDown() {
        ModelScanServiceFactory.setTestInstance(null);
    }

    // Test that the builder can be created and configured
    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        HLScanModelBuilder builder = new HLScanModelBuilder(
                modelName, hlClientId, hlClientSecret, folderToScan, failUnsupported, failSeverity);
        project.getBuildersList().add(builder);

        // Save and reload the project configuration
        project = jenkins.configRoundtrip(project);

        HLScanModelBuilder gotBuilder =
                (HLScanModelBuilder) project.getBuildersList().get(0);

        jenkins.assertEqualDataBoundBeans(builder, gotBuilder);
    }

    // Test that the builder can be created and run
    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        HLScanModelBuilder builder = createBuilder();
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(scanMessage, build);
    }

    // Test that the builder can be created and run in a scripted pipeline
    @Test
    public void testScriptedPipeline() throws Exception {
        String controllerLabel = jenkins.jenkins.getSelfLabel().getName();
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node('" + controllerLabel + "') {hlScanModel modelName: '" + modelName
                + "', hlClientId: '" + hlClientId
                + "', hlClientSecret: '" + hlClientSecret
                + "', folderToScan: '" + folderToScan
                + "', failOnUnsupported: " + failUnsupported
                + ", failOnSeverity: 'NONE"
                + "'}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains(scanMessage, completedBuild);

        // Add verification that mock was called with expected parameters
        verify(mockScannerService).scanFolder(eq(modelName), anyString());
    }

    @Test
    public void testCallableRunsOnRemoteAgent() throws Exception {
        Slave agent = jenkins.createOnlineSlave(Label.get("remote-scan"));
        FilePath folder = agent.getRootPath().child(folderToScan);
        folder.mkdirs();

        ScanResult result = folder.act(new HLScanModelBuilder.ScanFolderCallable(
                modelName,
                hlClientId,
                Secret.fromString(hlClientSecret),
                JenkinsProxySnapshot.none(),
                new FakeScannerService(modelName, modelId, modelVersion, scanId)));

        assertEquals(modelName, result.getModelName());
        assertEquals(modelVersion, result.getModelVersion());
        assertEquals(scanId, result.getScanId());
        assertEquals(ScanResult.Severity.SAFE, result.getSeverity().orElse(null));
    }

    private HLScanModelBuilder createBuilder() {
        return new HLScanModelBuilder(
                modelName, hlClientId, hlClientSecret, folderToScan, failUnsupported, failSeverity);
    }

    static ScanReport createScanReport(String modelName, String modelId, String modelVersion, String scanId) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2021-01-01T00:00:00Z");
        return ScanReport.builder()
                .detectionCount(0L)
                .fileCount(1L)
                .filesWithDetectionsCount(0L)
                .inventory(ScanReport.Inventory.builder()
                        .modelId(modelId)
                        .modelName(modelName)
                        .modelVersionId("version-id")
                        .requestedScanLocation("/path/to/model")
                        .modelVersion(modelVersion)
                        .build())
                .scanId(scanId)
                .startTime(offsetDateTime)
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
                .endTime(offsetDateTime)
                .severity(ScanReport.Severity.SAFE)
                .build();
    }

    static final class FakeScannerService implements HLScanModelBuilder.SerializableScannerService {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String modelName;
        private final String modelId;
        private final String modelVersion;
        private final String scanId;

        FakeScannerService(String modelName, String modelId, String modelVersion, String scanId) {
            this.modelName = modelName;
            this.modelId = modelId;
            this.modelVersion = modelVersion;
            this.scanId = scanId;
        }

        @Override
        public ScanReport scanFolder(String requestedModelName, String folderPath) {
            return createScanReport(modelName, modelId, modelVersion, scanId);
        }
    }
}
