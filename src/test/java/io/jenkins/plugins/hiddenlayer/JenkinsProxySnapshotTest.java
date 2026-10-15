package io.jenkins.plugins.hiddenlayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.ProxyConfiguration;
import hudson.util.Secret;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Base64;
import okhttp3.Authenticator;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class JenkinsProxySnapshotTest {

    @Test
    void noneIsNotConfigured() {
        assertFalse(JenkinsProxySnapshot.none().isConfigured());
        assertFalse(JenkinsProxySnapshot.from(null).isConfigured());
    }

    @Test
    void fromProxyConfigurationCapturesHostAndPort() {
        ProxyConfiguration config = new ProxyConfiguration("proxy.example.com", 3128);
        JenkinsProxySnapshot snapshot = JenkinsProxySnapshot.from(config);

        assertTrue(snapshot.isConfigured());
        Proxy proxy = snapshot.toProxy();
        assertEquals(Proxy.Type.HTTP, proxy.type());
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertEquals("proxy.example.com", address.getHostString());
        assertEquals(3128, address.getPort());
        assertNull(snapshot.toAuthenticator());
    }

    @Test
    void fromProxyConfigurationSkipsHiddenLayerNoProxyHost() {
        ProxyConfiguration config = new ProxyConfiguration("proxy.example.com", 8080, null, null, "api.hiddenlayer.ai");
        assertFalse(JenkinsProxySnapshot.from(config).isConfigured());
    }

    @Test
    void toAuthenticatorAddsProxyAuthorizationHeader() throws Exception {
        JenkinsProxySnapshot snapshot =
                new JenkinsProxySnapshot("proxy.example.com", 8080, "user", Secret.fromString("secret"));
        Authenticator authenticator = snapshot.toAuthenticator();
        assertNotNull(authenticator);

        Request originalRequest =
                new Request.Builder().url("https://api.hiddenlayer.ai/test").build();
        Response challengeResponse = new Response.Builder()
                .request(originalRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(407)
                .message("Proxy Authentication Required")
                .build();

        Request authenticatedRequest = authenticator.authenticate(null, challengeResponse);
        assertNotNull(authenticatedRequest);
        String decoded = new String(Base64.getDecoder()
                .decode(authenticatedRequest.header("Proxy-Authorization").substring(6)));
        assertEquals("user:secret", decoded);
    }

    @Test
    void roundTripsThroughJavaSerialization() throws Exception {
        JenkinsProxySnapshot original =
                new JenkinsProxySnapshot("proxy.example.com", 8080, "user", Secret.fromString("secret"));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }

        JenkinsProxySnapshot restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (JenkinsProxySnapshot) in.readObject();
        }

        assertTrue(restored.isConfigured());
        InetSocketAddress address = (InetSocketAddress) restored.toProxy().address();
        assertEquals("proxy.example.com", address.getHostString());
        assertEquals(8080, address.getPort());
        assertNotNull(restored.toAuthenticator());
    }
}
