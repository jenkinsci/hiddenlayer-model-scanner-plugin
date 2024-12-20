package com.hiddenlayer.jenkins;

import hiddenlayer.ModelScanService;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.openapitools.client.model.ScanReportV3;

/**
 * Jenkins Builder providing a build step to scan an ML model using the HiddenLayer Model Scanner.
 */
public class HLScanModelBuilder extends Builder implements SimpleBuildStep {

    // Name of the model to be scanned
    private String modelName;

    // HiddenLayer API credentials are needed to scan the model.
    // Credentials are the client ID and client secret.
    private String hlClientId;
    private String hlClientSecret;

    private String folderToScan;

    private ModelScanService modelScanService;

    @DataBoundConstructor
    public HLScanModelBuilder(String modelName, String hlClientId, String hlClientSecret, String folderToScan) {
        this.modelName = modelName;
        this.hlClientId = hlClientId;
        this.hlClientSecret = hlClientSecret;
        this.folderToScan = folderToScan;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getHlClientId() {
        return hlClientId;
    }

    public void setHlClientId(String hlClientId) {
        this.hlClientId = hlClientId;
    }

    public String getHlClientSecret() {
        return hlClientSecret;
    }

    public void setHlClientSecret(String hlClientSecret) {
        this.hlClientSecret = hlClientSecret;
    }

    public String getFolderToScan() {
        return folderToScan;
    }

    public void setFolderToScan(String folderToScan) {
        this.folderToScan = folderToScan;
    }

    public ModelScanService getModelScanService() {
        return modelScanService;
    }

    public void setModelScanService(ModelScanService modelScanService) {
        this.modelScanService = modelScanService;
    }

    /**
     * Executes the build step:
     * - Scan the ML model in the specified folder by calling the HiddenLayer Model Scanner
     * - Report results
     */
    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException, AbortException {
        // Keep this log message in sync with scanMessage in HLScanModelBuilderTest.java
        listener.getLogger().printf("Scanning model %s in folder %s\n", modelName, folderToScan);

        try {
            // Initialize the ModelScanService if needed (tests may inject a mock service)
            if (modelScanService == null) {
                modelScanService = ModelScanServiceFactory.getInstance(hlClientId, hlClientSecret);
            }

            // Scan the model in folderToScan
            FilePath folderPath = new FilePath(workspace, folderToScan);
            ScanReportV3 report = modelScanService.scanFolder(modelName, folderPath.getRemote());

            // TODO: don't just dump the report, print out what matters to the user:
            //  model name, model version, scan status, scan severity, console scan link, scan end time, scanner version
            //  include scanner error message if scan failed
            listener.getLogger().println("Scan complete. Results: " + report.toString());
            listener.getLogger().println("Web link to the HiddenLayer scan: " + getScanResultsUrl(report));

        } catch (Exception e) {
            listener.getLogger().println("Error scanning model: " + e.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            listener.getLogger().println(writer.toString());
            throw new AbortException("Error scanning model: " + e.getMessage());
        }
    }

    private String getScanResultsUrl(ScanReportV3 scanReport) {
        return "https://console.us.hiddenlayer.ai/model-details/"
                + scanReport.getInventory().getModelId() + "/scans/" + scanReport.getScanId();
    }

    @Symbol("hlScanModel")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            super(HLScanModelBuilder.class);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Scan ML Model with HiddenLayer";
        }

        // Form validations
        public FormValidation doCheckHlClientId(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.error("Client ID cannot be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckHlClientSecret(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.error("Client Secret cannot be empty");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckFolderToScan(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.error("Folder to scan cannot be empty");
            }
            return FormValidation.ok();
        }
    }
}
