package io.jenkins.hiddenlayer;

import com.hiddenlayer.sdk.ModelScanService;
import com.hiddenlayer.sdk.rest.models.ScanReportV3;
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
import hudson.util.Secret;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * HLScanModelBuilder provides a build step that scans an ML model using the HiddenLayer Model Scanner.
 */
public class HLScanModelBuilder extends Builder implements SimpleBuildStep {

    // Name of the model to be scanned
    private String modelName;

    // HiddenLayer API credentials are needed to scan the model.
    // Credentials are the client ID and client secret.
    private String hlClientId;
    private Secret hlClientSecret;

    // Folder containing the model to be scanned, relative to the workspace.
    private String folderToScan;

    // Service used to call the HiddenLayer Model Scanner.
    // Mark it as transient so it won't be serialized with the object, to avoid security problems.
    private transient ModelScanService modelScanService;

    @DataBoundConstructor
    public HLScanModelBuilder(String modelName, String hlClientId, String hlClientSecret, String folderToScan) {
        this.modelName = modelName;
        this.hlClientId = hlClientId;
        setHlClientSecret(hlClientSecret);
        this.folderToScan = folderToScan;
    }

    public String getModelName() {
        return modelName;
    }

    @DataBoundSetter
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getHlClientId() {
        return hlClientId;
    }

    @DataBoundSetter
    public void setHlClientId(String hlClientId) {
        this.hlClientId = hlClientId;
    }

    public String getHlClientSecret() {
        return Secret.toString(hlClientSecret);
    }

    @DataBoundSetter
    public void setHlClientSecret(String hlClientSecret) {
        this.hlClientSecret = Secret.fromString(hlClientSecret);
    }

    public String getFolderToScan() {
        return folderToScan;
    }

    @DataBoundSetter
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
     * Execute the build step:
     * - Scan the ML model in the specified folder by calling the HiddenLayer Model Scanner
     * - Report results
     */
    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException, AbortException {
        // Keep this log message in sync with scanMessage in HLScanModelBuilderTest.java
        listener.getLogger().printf("Scanning model %s in folder %s ...\n", modelName, folderToScan);

        try {
            // Initialize the ModelScanService if needed (tests may inject a mock service)
            if (modelScanService == null) {
                modelScanService = ModelScanServiceFactory.getInstance(hlClientId, hlClientSecret);
            }

            // Scan the model in folderToScan
            FilePath folderPath = new FilePath(workspace, folderToScan);
            String folder = folderPath.getRemote();
            ScanReportV3 report = modelScanService.scanFolder(folder, modelName);

            // Summarize the scan results for the user
            String summary = ScanReporter.summarizeScan(report);
            listener.getLogger().print(summary);

        } catch (Exception e) {
            listener.getLogger().println("Error scanning model: " + e.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            listener.getLogger().println(writer.toString());
            throw new AbortException("Error scanning model: " + e.getMessage());
        }
    }

    @Extension
    @Symbol("hlScanModel")
    /**
     * DescriptorImpl is a descriptor class for a Builder (a build step that runs during the build process).
     */
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

        public FormValidation doCheckModelName(@QueryParameter String value) {
            if (value.trim().isEmpty()) {
                return FormValidation.error("Client ID cannot be empty");
            }
            return FormValidation.ok();
        }

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
