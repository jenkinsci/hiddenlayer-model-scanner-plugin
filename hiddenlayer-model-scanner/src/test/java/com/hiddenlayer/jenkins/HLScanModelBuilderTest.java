package com.hiddenlayer.jenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class HLScanModelBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String hlClientId = "client-id";
    final String hlClientSecret = "client-secret";
    final String folderToScan = "folder-to-scan";

    // This text is printed to the console when the plugin is run.
    final String scanMessage = String.format("Scanning folder: %s", folderToScan);

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

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript = "node {hlScanModel hlClientId: '" + hlClientId + "', hlClientSecret: '" + hlClientSecret
                + "', folderToScan: '" + folderToScan + "'}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkins.assertLogContains(scanMessage, completedBuild);
    }

    private HLScanModelBuilder createBuilder() {
        return new HLScanModelBuilder(hlClientId, hlClientSecret, folderToScan);
    }
}
