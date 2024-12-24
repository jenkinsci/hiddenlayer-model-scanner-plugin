package io.jenkins.hiddenlayer;

import com.hiddenlayer.sdk.Configuration;
import com.hiddenlayer.sdk.ModelScanService;
import hudson.Extension;
import hudson.util.Secret;

@Extension
public class ModelScanServiceFactory {
    private static ModelScanService instance;

    public static void setTestInstance(ModelScanService service) {
        instance = service;
    }

    public static ModelScanService getInstance(String clientId, Secret clientSecret) throws Exception {
        if (instance != null) {
            return instance;
        }
        Configuration config = new Configuration(clientId, clientSecret.getPlainText());
        return new ModelScanService(config);
    }
}
