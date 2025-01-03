package io.jenkins.hiddenlayer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hiddenlayer.sdk.ModelScanService;
import com.hiddenlayer.sdk.rest.models.ModelInventoryInfo;
import com.hiddenlayer.sdk.rest.models.ScanReportV3;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
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
    final String failSeverity = "";

    // This text is printed to the console when the plugin is run.
    final String scanMessage = String.format("Scanning model %s in folder %s", modelName, folderToScan);

    // Test objects
    private ModelScanService mockModelScanService;
    private String modelVersion = "1.0.0";
    private String modelId = "model-id";
    private String scanId = "scan-id";

    @Before
    public void setUp() throws IOException, URISyntaxException, InterruptedException, Exception {
        mockModelScanService = mock(ModelScanService.class);

        ModelInventoryInfo mii = new ModelInventoryInfo();
        mii.setModelName(modelName);
        mii.setModelVersion(modelVersion);
        mii.setModelId(modelId);

        ScanReportV3 scanReport = new ScanReportV3();
        scanReport.setStatus(ScanReportV3.StatusEnum.DONE);
        scanReport.setInventory(mii);
        scanReport.setScanId(scanId);
        scanReport.setSeverity(ScanReportV3.SeverityEnum.SAFE);
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2021-01-01T00:00:00Z");
        scanReport.setEndTime(offsetDateTime);
        scanReport.setVersion("24.10.2");

        when(mockModelScanService.scanFolder(anyString(), anyString(), eq(true)))
                .thenReturn(scanReport);
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

        // Set mock service after roundtrip
        HLScanModelBuilder gotBuilder =
                (HLScanModelBuilder) project.getBuildersList().get(0);
        gotBuilder.setModelScanService(mockModelScanService);

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
        // Set up the mock service globally
        ModelScanServiceFactory.setTestInstance(mockModelScanService);

        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel)); // this Jenkins method name needs updating
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {hlScanModel modelName: '" + modelName
                + "', hlClientId: '" + hlClientId
                + "', hlClientSecret: '" + hlClientSecret
                + "', folderToScan: '" + folderToScan
                + "', failOnUnsupported: " + failUnsupported
                + ", failOnSeverity: '" + failSeverity
                + "'}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains(scanMessage, completedBuild);

        // Add verification that mock was called with expected parameters
        verify(mockModelScanService).scanFolder(anyString(), eq(modelName), eq(true));
    }

    private HLScanModelBuilder createBuilder() {
        HLScanModelBuilder builder = new HLScanModelBuilder(
                modelName, hlClientId, hlClientSecret, folderToScan, failUnsupported, failSeverity);
        builder.setModelScanService(mockModelScanService);
        return builder;
    }
}
