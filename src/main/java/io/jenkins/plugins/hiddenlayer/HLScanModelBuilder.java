package io.jenkins.plugins.hiddenlayer;

import com.hiddenlayer.api.models.scans.results.ScanReport;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
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
import java.io.Serial;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Optional;
import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
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
            FilePath folderPath = new FilePath(workspace, folderToScan);
            ScanResult report = folderPath.act(
                    new ScanFolderCallable(modelName, hlClientId, hlClientSecret, JenkinsProxySnapshot.fromJenkins()));

            // Summarize the scan results for the user
            String summary = ScanReporter.summarizeScan(report);
            listener.getLogger().print(summary);
            Optional<String> unrecognizedSeverity = report.getUnrecognizedSeverity();
            unrecognizedSeverity.ifPresent(
                    raw -> listener.getLogger().printf("Unrecognized scan severity from HiddenLayer: %s%n", raw));
            ScanResult.Severity reportSeverity = report.getSeverity().orElse(null);
            if (failOnUnsupported
                    && unrecognizedSeverity.isEmpty()
                    && (reportSeverity == null || ScanResult.Severity.UNKNOWN.equals(reportSeverity))) {
                throw new AbortException("Model type is not supported by HiddenLayer");
            }
            if (failOnSeverity != FailOnDetectionSeverityEnum.NONE && reportSeverity != null) {
                // just kick out if SAFE or UNKNOWN
                if (!ScanResult.Severity.UNKNOWN.equals(reportSeverity)
                        && !ScanResult.Severity.SAFE.equals(reportSeverity)) {
                    if (ScanResult.Severity.LOW.equals(reportSeverity)) {
                        if (failOnSeverity == FailOnDetectionSeverityEnum.LOW) {
                            listener.getLogger()
                                    .printf(
                                            "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                            reportSeverity, failOnSeverity);
                            throw new AbortException("Model has " + reportSeverity + " severity detection!");
                        }
                    } else if (ScanResult.Severity.MEDIUM.equals(reportSeverity)) {
                        if (failOnSeverity == FailOnDetectionSeverityEnum.MEDIUM
                                || failOnSeverity == FailOnDetectionSeverityEnum.LOW) {
                            listener.getLogger()
                                    .printf(
                                            "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                            reportSeverity, failOnSeverity);
                            throw new AbortException("Model has " + reportSeverity + " severity detection!");
                        }
                    } else if (ScanResult.Severity.HIGH.equals(reportSeverity)) {
                        if (failOnSeverity == FailOnDetectionSeverityEnum.HIGH
                                || failOnSeverity == FailOnDetectionSeverityEnum.MEDIUM
                                || failOnSeverity == FailOnDetectionSeverityEnum.LOW) {
                            listener.getLogger()
                                    .printf(
                                            "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                            reportSeverity, failOnSeverity);
                            throw new AbortException("Model has " + reportSeverity + " severity detection!");
                        }
                    } else if (ScanResult.Severity.CRITICAL.equals(reportSeverity)) {
                        listener.getLogger()
                                .printf(
                                        "Failing build due to model scan having a %s severity detection (threshold: %s)%n",
                                        reportSeverity, failOnSeverity);
                        throw new AbortException("Model has " + reportSeverity + " severity detection!");
                    } else {
                        listener.getLogger().println("Model has unknown severity level");
                    }
                }
            }
        } catch (AbortException e) {
            throw e;
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            listener.getLogger().println("Error scanning model: " + e.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            listener.getLogger().println(writer);
            throw new AbortException("Error scanning model: " + e.getMessage());
        }
    }

    /**
     * Remoting-safe scanner used by tests. Production always constructs the client via
     * {@link ModelScanServiceFactory} on the node that owns the workspace.
     */
    interface SerializableScannerService extends ScannerService, Serializable {}

    /**
     * Runs the scan on the node that owns the workspace. Must be a static class so Jenkins remoting
     * can serialize it to remote agents without capturing {@link HLScanModelBuilder} or {@link TaskListener}.
     */
    static final class ScanFolderCallable extends MasterToSlaveFileCallable<ScanResult> {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String modelName;
        private final String clientId;
        private final Secret clientSecret;
        private final JenkinsProxySnapshot proxySnapshot;
        private final SerializableScannerService scannerOverride;

        ScanFolderCallable(String modelName, String clientId, Secret clientSecret) {
            this(modelName, clientId, clientSecret, JenkinsProxySnapshot.none(), null);
        }

        ScanFolderCallable(String modelName, String clientId, Secret clientSecret, JenkinsProxySnapshot proxySnapshot) {
            this(modelName, clientId, clientSecret, proxySnapshot, null);
        }

        ScanFolderCallable(
                String modelName,
                String clientId,
                Secret clientSecret,
                JenkinsProxySnapshot proxySnapshot,
                SerializableScannerService scannerOverride) {
            this.modelName = modelName;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.proxySnapshot = proxySnapshot != null ? proxySnapshot : JenkinsProxySnapshot.none();
            this.scannerOverride = scannerOverride;
        }

        @Override
        public ScanResult invoke(File f, VirtualChannel channel) throws IOException {
            try {
                ScannerService scanner = scannerOverride != null
                        ? scannerOverride
                        : ModelScanServiceFactory.getInstance(clientId, clientSecret, proxySnapshot);
                ScanReport report = scanner.scanFolder(modelName, f.getAbsolutePath());
                return ScanResult.from(report);
            } catch (RuntimeException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    /**
     * DescriptorImpl is a descriptor class for a Builder (a build step that runs during the build process).
     */
    @Extension
    @Symbol("hlScanModel")
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
