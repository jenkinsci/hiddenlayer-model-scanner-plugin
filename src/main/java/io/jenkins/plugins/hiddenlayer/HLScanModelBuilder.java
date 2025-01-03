package io.jenkins.plugins.hiddenlayer;

import com.hiddenlayer.sdk.ModelScanService;
import com.hiddenlayer.sdk.rest.models.ScanReportV3;
import com.hiddenlayer.sdk.rest.models.ScanReportV3.SeverityEnum;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import jenkins.security.Roles;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

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

    // Fail the build if the model is of an unsupported type
    private boolean failOnUnsupported;

    // Fail the build if the model has a severity level greater than or equal to the specified level
    private FailOnDetectionSeverityEnum failOnSeverity;

    // Service used to call the HiddenLayer Model Scanner.
    // Mark it as transient so it won't be serialized with the object, to avoid security problems.
    private transient ModelScanService modelScanService;

    @DataBoundConstructor
    public HLScanModelBuilder(
            String modelName,
            String hlClientId,
            String hlClientSecret,
            String folderToScan,
            boolean failOnUnsupported,
            FailOnDetectionSeverityEnum failOnSeverity) {
        this.modelName = modelName;
        this.hlClientId = hlClientId;
        setHlClientSecret(hlClientSecret);
        this.folderToScan = folderToScan;
        this.failOnUnsupported = failOnUnsupported;
        this.failOnSeverity = failOnSeverity;
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

    public Secret getHlClientSecret() {
        return hlClientSecret;
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

    public boolean getFailOnUnsupported() {
        return failOnUnsupported;
    }

    @DataBoundSetter
    public void setFailOnUnsupported(boolean failOnUnsupported) {
        this.failOnUnsupported = failOnUnsupported;
    }

    public FailOnDetectionSeverityEnum getFailOnSeverity() {
        return failOnSeverity;
    }

    @DataBoundSetter
    public void setFailOnSeverity(FailOnDetectionSeverityEnum failOnSeverity) {
        this.failOnSeverity = failOnSeverity;
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
        listener.getLogger().printf("Scanning model %s in folder %s ...%n", modelName, folderToScan);

        try {
            // Initialize the ModelScanService if needed (tests may inject a mock service)
            if (modelScanService == null) {
                modelScanService = ModelScanServiceFactory.getInstance(hlClientId, hlClientSecret);
            }

            // Scan the model in folderToScan
            FilePath folderPath = new FilePath(workspace, folderToScan);
            ScanReportV3 report = folderPath.act(new FileCallable<ScanReportV3>() {
                private static final long serialVersionUID = 1L;

                @Override
                public ScanReportV3 invoke(File f, VirtualChannel channel) {
                    String folder = f.getAbsolutePath();
                    try {
                        // The last arg to scanFolder is waitForDone
                        ScanReportV3 report = modelScanService.scanFolder(folder, modelName, true);
                        return report;
                    } catch (Exception e) {
                        listener.getLogger().println("Error scanning model: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void checkRoles(RoleChecker checker) throws SecurityException {
                    checker.check(this, Roles.SLAVE);
                }
            });

            // Summarize the scan results for the user
            String summary = ScanReporter.summarizeScan(report);
            listener.getLogger().print(summary);
            if (failOnUnsupported && report.getSeverity() == SeverityEnum.UNKNOWN) {
                throw new AbortException("Model type is not supported by HiddenLayer");
            }
            if (failOnSeverity != FailOnDetectionSeverityEnum.NONE) {
                SeverityEnum reportSeverity = report.getSeverity();
                // just kick out if SAFE or UNKNOWN
                if (reportSeverity != SeverityEnum.UNKNOWN && reportSeverity != SeverityEnum.SAFE) {
                    switch (reportSeverity) {
                        case LOW:
                            if (failOnSeverity == FailOnDetectionSeverityEnum.LOW) {
                                listener.getLogger()
                                        .printf(
                                                "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                                reportSeverity, failOnSeverity);
                                throw new AbortException("Model has " + reportSeverity + " severity detection!");
                            }
                            break;
                        case MEDIUM:
                            if (failOnSeverity == FailOnDetectionSeverityEnum.MEDIUM
                                    || failOnSeverity == FailOnDetectionSeverityEnum.LOW) {
                                listener.getLogger()
                                        .printf(
                                                "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                                reportSeverity, failOnSeverity);
                                throw new AbortException("Model has " + reportSeverity + " severity detection!");
                            }
                            break;
                        case HIGH:
                            if (failOnSeverity == FailOnDetectionSeverityEnum.HIGH
                                    || failOnSeverity == FailOnDetectionSeverityEnum.MEDIUM
                                    || failOnSeverity == FailOnDetectionSeverityEnum.LOW) {
                                listener.getLogger()
                                        .printf(
                                                "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                                reportSeverity, failOnSeverity);
                                throw new AbortException("Model has " + reportSeverity + " severity detection!");
                            }
                            break;
                        case CRITICAL:
                            listener.getLogger()
                                    .printf(
                                            "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                            reportSeverity, failOnSeverity);
                            throw new AbortException("Model has " + reportSeverity + " severity detection!");
                        default:
                            listener.getLogger().println("Model has unknown severity level");
                            break;
                    }
                }
            }
        } catch (AbortException e) {
            throw e;
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

        @POST
        public FormValidation doCheckModelName(@QueryParameter String value, @AncestorInPath Item item) {
            item.checkPermission(Item.CONFIGURE);
            if (value.trim().isEmpty()) {
                return FormValidation.error("Model Name cannot be empty");
            }
            return FormValidation.ok();
        }

        @POST
        public FormValidation doCheckHlClientId(@QueryParameter String value, @AncestorInPath Item item) {
            item.checkPermission(Item.CONFIGURE);
            if (value.trim().isEmpty()) {
                return FormValidation.error("Client ID cannot be empty");
            }
            return FormValidation.ok();
        }

        @POST
        public FormValidation doCheckHlClientSecret(@QueryParameter String value, @AncestorInPath Item item) {
            item.checkPermission(Item.CONFIGURE);
            if (value.trim().isEmpty()) {
                return FormValidation.error("Client Secret cannot be empty");
            }
            return FormValidation.ok();
        }

        @POST
        public FormValidation doCheckFolderToScan(@QueryParameter String value, @AncestorInPath Item item) {
            item.checkPermission(Item.CONFIGURE);
            if (value.trim().isEmpty()) {
                return FormValidation.error("Folder to scan cannot be empty");
            }
            return FormValidation.ok();
        }
    }
}
