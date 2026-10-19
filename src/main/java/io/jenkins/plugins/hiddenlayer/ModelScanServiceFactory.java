package io.jenkins.plugins.hiddenlayer;

import com.hiddenlayer.api.client.HiddenLayerClient;
import com.hiddenlayer.api.client.HiddenLayerClientImpl;
import com.hiddenlayer.api.core.ClientOptions;
import hudson.util.Secret;
import java.util.regex.Pattern;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class ModelScanServiceFactory {

    private static ScannerService instance;

    public static void setTestInstance(ScannerService scanner) {
        instance = scanner;
    }

    public static ScannerService getInstance(String clientId, Secret clientSecret) {
        return getInstance(clientId, clientSecret, JenkinsProxySnapshot.fromJenkins());
    }

    public static ScannerService getInstance(String clientId, Secret clientSecret, JenkinsProxySnapshot proxySnapshot) {
        if (instance != null) {
            return instance;
        }

        JenkinsHttpClient.Builder httpClientBuilder = JenkinsHttpClient.builder();
        JenkinsProxySnapshot proxy = proxySnapshot != null ? proxySnapshot : JenkinsProxySnapshot.none();
        if (proxy.isConfigured()) {
            httpClientBuilder.proxy(proxy.toProxy());
            Authenticator proxyAuth = proxy.toAuthenticator();
            if (proxyAuth != null) {
                httpClientBuilder.proxyAuthenticator(proxyAuth);
            }
        }

        ClientOptions clientOptions = ClientOptions.builder()
                .httpClient(httpClientBuilder.build())
                .clientId(clientId)
                .clientSecret(clientSecret.getPlainText())
                .build();

        HiddenLayerClient client = new HiddenLayerClientImpl(clientOptions);
        return new ModelScannerWrapper(client.modelScanner());
    }

    static boolean matchesNoProxyPattern(Iterable<Pattern> patterns, String host) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(host).matches()) {
                return true;
            }
        }
        return false;
    }

    static Authenticator createProxyAuthenticator(String username, String password) {
        return new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) {
                String credential = Credentials.basic(username, password);
                return response.request()
                        .newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            }
        };
    }
}
