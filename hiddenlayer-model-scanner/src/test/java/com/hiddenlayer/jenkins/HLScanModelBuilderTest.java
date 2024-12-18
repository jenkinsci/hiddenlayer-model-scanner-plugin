package com.hiddenlayer.jenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class HLScanModelBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String hlClientId = "client-id";
    final String hlClientSecretCredentialId = "credential-id";
    final String folderToScan = "folder-to-scan";

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
        jenkins.assertLogContains("Scanning folder", build);
    }

    private HLScanModelBuilder createBuilder() {
        return new HLScanModelBuilder(hlClientId, hlClientSecretCredentialId, folderToScan);
    }

}
