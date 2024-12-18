package com.hiddenlayer.jenkins;

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
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * 
 */
public class HLScanModelBuilder extends Builder implements SimpleBuildStep {

    // HiddenLayer API credentials are needed to scan the model.
    // Credentials are the client ID and client secret.
    private String hlClientId;
    private String hlClientSecret;

    private String folderToScan;

    @DataBoundConstructor
    public HLScanModelBuilder(String hlClientId, String hlClientSecret, String folderToScan) {
        this.hlClientId = hlClientId;
        this.hlClientSecret = hlClientSecret;
        this.folderToScan = folderToScan;

    }

    public String getHlClientId() {
        return hlClientId;
    }

    @DataBoundSetter
    public void setHlClientId(String hlClientId) {
        this.hlClientId = hlClientId;
    }

    public String getHlClientSecret() {
        return hlClientSecret;
    }

    @DataBoundSetter
    public void setHlClientSecret(String hlClientSecret) {
        this.hlClientSecret = hlClientSecret;
    }

    public String getFolderToScan() {
        return folderToScan;
    }

    @DataBoundSetter
    public void setFolderToScan(String folderToScan) {
        this.folderToScan = folderToScan;
    }

    /** 
     * Executes the build step:
     * - Scan the ML model in the specified folder by calling the HiddenLayer Model Scanner
     * - Report results
     */
    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println("Scanning folder: " + folderToScan);
        // Add scanning and reporting logic here
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