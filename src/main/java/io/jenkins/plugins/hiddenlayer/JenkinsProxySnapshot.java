package io.jenkins.plugins.hiddenlayer;

import hudson.ProxyConfiguration;
import hudson.util.Secret;
import java.io.Serial;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import jenkins.model.Jenkins;
import okhttp3.Authenticator;

/**
 * Serializes Jenkins controller proxy settings so a remote agent can reach HiddenLayer without
 * calling {@link Jenkins#get()} (which is null on agents).
 */
public final class JenkinsProxySnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    static final String HIDDENLAYER_API_HOST = "api.hiddenlayer.ai";

    private static final JenkinsProxySnapshot NONE = new JenkinsProxySnapshot(null, 0, null, null);

    private final String host;
    private final int port;
    private final String username;
    private final Secret password;

    JenkinsProxySnapshot(String host, int port, String username, Secret password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public static JenkinsProxySnapshot none() {
        return NONE;
    }

    public static JenkinsProxySnapshot fromJenkins() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return none();
        }
        return from(jenkins.proxy);
    }

    static JenkinsProxySnapshot from(ProxyConfiguration proxyConfig) {
        if (proxyConfig == null) {
            return none();
        }
        if (ModelScanServiceFactory.matchesNoProxyPattern(proxyConfig.getNoProxyHostPatterns(), HIDDENLAYER_API_HOST)) {
            return none();
        }
        String proxyHost = proxyConfig.name;
        if (proxyHost == null || proxyHost.isEmpty()) {
            return none();
        }
        return new JenkinsProxySnapshot(
                proxyHost, proxyConfig.port, proxyConfig.getUserName(), proxyConfig.getSecretPassword());
    }

    public boolean isConfigured() {
        return host != null && !host.isEmpty();
    }

    public Proxy toProxy() {
        if (!isConfigured()) {
            throw new IllegalStateException("No proxy is configured");
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    }

    public Authenticator toAuthenticator() {
        if (username == null || username.isEmpty()) {
            return null;
        }
        String passwordStr = password != null ? password.getPlainText() : "";
        return ModelScanServiceFactory.createProxyAuthenticator(username, passwordStr);
    }
}
