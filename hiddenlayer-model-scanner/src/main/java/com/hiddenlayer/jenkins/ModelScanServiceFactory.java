package com.hiddenlayer.jenkins;

import hiddenlayer.Config;
import hiddenlayer.ModelScanService;
import hudson.Extension;

@Extension
public class ModelScanServiceFactory {
    private static ModelScanService instance;

    public static void setTestInstance(ModelScanService service) {
        instance = service;
    }

    public static ModelScanService getInstance(String clientId, String clientSecret) throws Exception {
        if (instance != null) {
            return instance;
        }
        Config config = new Config(clientId, clientSecret);
        return new ModelScanService(config);
    }
}
