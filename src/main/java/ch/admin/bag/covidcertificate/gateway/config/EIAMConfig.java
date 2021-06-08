package ch.admin.bag.covidcertificate.gateway.config;

import ch.admin.bag.covidcertificate.gateway.client.eiam.AdvancedHttpClientFactoryBean;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.JeapSaajSoapMessageFactory;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
public class EIAMConfig {
    private static final String CONTEXT_PATH = "ch.admin.bag.covidcertificate.gateway.eiam.adminservice";
    private static final String KEYSTORE_TYPE = "pkcs12";
    private static final String TRUSTSTORE_TYPE = "jks";
    @Value("${eiam-admin-service.url}")
    private String url;
    @Value("${app.conn.eiam-admin-service.key-store-password}")
    private String keystorePassword;
    @Value("${app.conn.eiam-admin-service.trust-store-password}")
    private String truststorePassword;

    @Bean
    public EIAMClient getEIAMClient(
            Jaxb2Marshaller marshaller,
            HttpComponentsMessageSender messageSender,
            PayloadValidatingInterceptor interceptor,
            JeapSaajSoapMessageFactory messageFactory) {
        EIAMClient eiamClient = new EIAMClient();
        eiamClient.setDefaultUri(url);
        eiamClient.setMarshaller(marshaller);
        eiamClient.setUnmarshaller(marshaller);
        eiamClient.setMessageSender(messageSender);
        eiamClient.setInterceptors(new PayloadValidatingInterceptor[]{interceptor});
        eiamClient.setMessageFactory(messageFactory);
        return eiamClient;
    }

    @Bean
    public Jaxb2Marshaller getJaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CONTEXT_PATH);
        return marshaller;
    }

    @Bean
    public HttpComponentsMessageSender getHttpComponentsMessageSender(HttpClient httpClient) {
        return new HttpComponentsMessageSender(httpClient);
    }

    @Bean
    public HttpClient getHttpClient() throws Exception {
        AdvancedHttpClientFactoryBean httpClientFactory = new AdvancedHttpClientFactoryBean();
        httpClientFactory.setKeystoreType(KEYSTORE_TYPE);
        // TODO: Insert value.
        httpClientFactory.setKeystoreLocation(new FileSystemResource(""));
        httpClientFactory.setKeystorePassword(keystorePassword);
        httpClientFactory.setTruststoreType(TRUSTSTORE_TYPE);
        // TODO: Insert value.
        httpClientFactory.setTruststoreLocation(new FileSystemResource(""));
        httpClientFactory.setTruststorePassword(truststorePassword);
        httpClientFactory.setMaxConnTotal(10);
        httpClientFactory.setConnectTimeout(1000);
        httpClientFactory.setAllowAllHostnameVerifier(true);
        return httpClientFactory.getObject();
    }

    @Bean
    public PayloadValidatingInterceptor getPayloadValidatingInterceptor() {
        PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
        // TODO: Insert value.
        interceptor.setSchema(new FileSystemResource(""));
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        return interceptor;
    }

    @Bean
    public JeapSaajSoapMessageFactory getJeapSaajSoapMessageFactory() {
        return new JeapSaajSoapMessageFactory();
    }
}
