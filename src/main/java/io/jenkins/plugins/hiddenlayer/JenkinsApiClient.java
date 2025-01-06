package io.jenkins.plugins.hiddenlayer;

import com.hiddenlayer.sdk.rest.ApiClient;
import hudson.ProxyConfiguration;

public class JenkinsApiClient extends ApiClient {
    public JenkinsApiClient() {
        super(ProxyConfiguration.newHttpClientBuilder(), null, null);
        this.setObjectMapper(createDefaultObjectMapper());
    }
}
