package ch.admin.bag.covidcertificate.gateway.client.eiam;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;

public class AdvancedHttpClientFactoryBean implements FactoryBean<HttpClient> {

    private static final String DEFAULT_TYPE = "jks";

    private String keystoreType = DEFAULT_TYPE;

    private Resource keystoreLocation;

    private String keystorePassword = null;

    private Resource truststoreLocation;

    private String truststorePassword = null;

    private String truststoreType = DEFAULT_TYPE;

    private boolean allowAllHostnameVerifier = true;

    private int connectTimeout = -1;

    private int readTimeout = -1;

    private int maxConnTotal = 50;

    private Credentials credentials;

    private HttpHost proxyHost;

    @Override
    public HttpClient getObject() throws Exception {
        var httpClientBuilder = HttpClientBuilder.create();
        handleInterceptors(httpClientBuilder);
        handleSSLConnection(httpClientBuilder);
        handleRequestConfig(httpClientBuilder);
        handleProxyConfig(httpClientBuilder);
        handleHostnameVerifier(httpClientBuilder);
        handleUsernameAndPasswordCredential(httpClientBuilder);
        handleConnectionPoolConfig(httpClientBuilder);
        return httpClientBuilder.build();
    }

    @Override
    public Class<HttpClient> getObjectType() {
        return HttpClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    protected void handleHostnameVerifier(HttpClientBuilder httpClientBuilder) {
        if (allowAllHostnameVerifier) {
            httpClientBuilder.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
    }

    protected void handleInterceptors(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder.addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor());
    }

    protected void handleUsernameAndPasswordCredential(HttpClientBuilder httpClientBuilder) {
        if (credentials != null) {
            CredentialsProvider credPv = new BasicCredentialsProvider();
            credPv.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(credPv);
        }
    }

    protected void handleConnectionPoolConfig(HttpClientBuilder httpClientBuilder) {
        httpClientBuilder.setMaxConnTotal(maxConnTotal);
    }

    protected void handleProxyConfig(HttpClientBuilder httpClientBuilder) {
        if (proxyHost != null) {
            httpClientBuilder.setProxy(proxyHost);
        }
    }

    protected void handleRequestConfig(HttpClientBuilder httpClientBuilder) {
        var requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .build();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);
    }

    protected void handleSSLConnection(HttpClientBuilder httpClientBuilder) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        if (this.keystoreLocation != null || this.truststoreLocation != null) {
            var sslContextBuilder = SSLContexts.custom();
            if (this.keystoreLocation != null) {
                var keystore = getKeyStore();
                sslContextBuilder.loadKeyMaterial(keystore, this.keystorePassword.toCharArray());
            }
            if (this.truststoreLocation != null) {
                var truststore = getTruststore();
                sslContextBuilder.loadTrustMaterial(truststore);
            }
            httpClientBuilder.setSslcontext(sslContextBuilder.build());
        }
    }

    private KeyStore getTruststore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        var truststore = KeyStore.getInstance(truststoreType);
        try (var fis = truststoreLocation.getInputStream()) {
            truststore.load(fis, this.truststorePassword.toCharArray());
        }
        return truststore;
    }

    private KeyStore getKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        var keystore = KeyStore.getInstance(keystoreType);
        try (var fis = keystoreLocation.getInputStream()) {
            keystore.load(fis, this.keystorePassword.toCharArray());
        }
        return keystore;
    }

    public void setKeystoreType(String keystoreType) {
        Objects.requireNonNull(keystoreType, "keystoreType must not be null");
        this.keystoreType = keystoreType;
    }

    public void setKeystoreLocation(Resource keystoreLocation) {
        Objects.requireNonNull(keystoreLocation, "keystoreType must not be null");
        Assert.isTrue(keystoreLocation.exists(), "keystoreType must not exists " + keystoreLocation);
        this.keystoreLocation = keystoreLocation;
    }


    public void setKeystorePassword(String keystorePassword) {
        Objects.requireNonNull(keystorePassword, "keystorePassword must not be null");
        this.keystorePassword = keystorePassword;
    }


    public void setTruststoreLocation(Resource truststoreLocation) {
        this.truststoreLocation = truststoreLocation;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public void setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
    }

    public void setAllowAllHostnameVerifier(boolean allowAllHostnameVerifier) {
        this.allowAllHostnameVerifier = allowAllHostnameVerifier;
    }

    /**
     * Sets the timeout until a connection is established. A value of -1 means <em>never</em> timeout.
     *
     * @param connectTimeout the timeout value in milliseconds
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Set the socket read timeout for the underlying HttpClient. A value of -1 means <em>never</em> timeout.
     *
     * @param readTimeout the timeout value in milliseconds
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setProxyHost(HttpHost proxyHost) {
        this.proxyHost = proxyHost;
    }
}
