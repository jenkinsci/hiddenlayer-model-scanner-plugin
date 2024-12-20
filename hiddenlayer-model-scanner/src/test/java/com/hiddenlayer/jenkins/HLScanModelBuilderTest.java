package com.hiddenlayer.jenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
//import org.mockito.ArgumentCaptor;
import org.openapitools.client.model.ModelInventoryInfo;
import org.openapitools.client.model.ScanReportV3;

import hiddenlayer.ModelScanService;
import hiddenlayer.sdk.ApiException;

public class HLScanModelBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    // Builder parameters
    final String modelName = "model-name";
    final String hlClientId = "client-id";
    final String hlClientSecret = "client-secret";
    final String folderToScan = "folder-to-scan";

    // This text is printed to the console when the plugin is run.
    final String scanMessage = String.format("Scanning model %s in folder %s", modelName, folderToScan);

    // Test objects
    private ModelScanService mockModelScanService;
    private String modelVersion = "1.0.0";
    private String modelId = "model-id";
    private String scanId = "scan-id";

    @Before
    public void setUp() throws ApiException, IOException, URISyntaxException, InterruptedException, Exception {
        mockModelScanService = mock(ModelScanService.class);

        ModelInventoryInfo mii = new ModelInventoryInfo();
        mii.setModelName(modelName);
        mii.setModelVersion(modelVersion);
        mii.setModelId(modelId);

        ScanReportV3 scanReport = new ScanReportV3();
        scanReport.setInventory(mii);
        scanReport.setScanId(scanId);
        scanReport.setSeverity(ScanReportV3.SeverityEnum.SAFE);
    
        // TODO: capture and validate the arguments passed to the scanFolder method
        // Not all tests call scanFolder, though, so we can't require the args in all tests.
        when(mockModelScanService.scanFolder(anyString(), anyString())).thenReturn(scanReport);
    }

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        HLScanModelBuilder builder = createBuilder();
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(builder, project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        HLScanModelBuilder builder = createBuilder();
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains(scanMessage, build);
    }

    // @Test
    // public void testScriptedPipeline() throws Exception {
    //     String agentLabel = "my-agent";
    //     jenkins.createOnlineSlave(Label.get(agentLabel));
    //     WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
    //     String pipelineScript = "node {hlScanModel modelName: '" + modelName
    //             + "', hlClientId: '" + hlClientId
    //             + "', hlClientSecret: '" + hlClientSecret
    //             + "', folderToScan: '" + folderToScan + "'}";
    //     job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
    //     WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
    //     jenkins.assertLogContains(scanMessage, completedBuild);
    // }

    private HLScanModelBuilder createBuilder() {
        HLScanModelBuilder builder = new HLScanModelBuilder(modelName, hlClientId, hlClientSecret, folderToScan);
        builder.setModelScanService(mockModelScanService);
        return builder;
    }
}
