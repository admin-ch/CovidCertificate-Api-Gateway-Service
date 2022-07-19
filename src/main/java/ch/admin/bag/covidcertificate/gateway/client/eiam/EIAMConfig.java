package ch.admin.bag.covidcertificate.gateway.client.eiam;

import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import io.jsonwebtoken.io.Decoders;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
@Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
public class EIAMConfig {
    public static final String CLIENT_NAME = "GGG";

    private static final String CONTEXT_PATH = "ch.admin.bag.covidcertificate.gateway.eiam.adminservice";
    private static final String KEYSTORE_TYPE = "pkcs12";
    private static final String TRUSTSTORE_TYPE = "jks";
    @Value("${eiam-admin-service.url}")
    private String url;
    @Value("${eiam-admin-service.keystore}")
    private String keystore;
    @Value("${eiam-admin-service.keystore-password}")
    private String keystorePassword;
    @Value("${cc-api-gateway-service.truststore.password}")
    private String truststorePassword;

    @Bean
    public EIAMClient getEIAMClient(
            Jaxb2Marshaller marshaller,
            HttpComponentsMessageSender messageSender,
            PayloadValidatingInterceptor interceptor,
            JeapSaajSoapMessageFactory messageFactory) {
        var eiamClient = new EIAMClient();
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
        var marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CONTEXT_PATH);
        return marshaller;
    }

    @Bean
    public HttpComponentsMessageSender getHttpComponentsMessageSender(HttpClient httpClient) {
        return new HttpComponentsMessageSender(httpClient);
    }

    @Bean
    public HttpClient getHttpClient() throws Exception {
        var httpClientFactory = new AdvancedHttpClientFactoryBean();
        httpClientFactory.setKeystoreType(KEYSTORE_TYPE);
        httpClientFactory.setKeystoreLocation(new ByteArrayResource(Decoders.BASE64.decode(keystore)));
        httpClientFactory.setKeystorePassword(keystorePassword);
        httpClientFactory.setTruststoreType(TRUSTSTORE_TYPE);
        httpClientFactory.setTruststoreLocation(new ClassPathResource("truststore.jks"));
        httpClientFactory.setTruststorePassword(truststorePassword);
        httpClientFactory.setMaxConnTotal(10);
        httpClientFactory.setConnectTimeout(1000);
        httpClientFactory.setAllowAllHostnameVerifier(true);
        return httpClientFactory.getObject();
    }

    @Bean
    public PayloadValidatingInterceptor getPayloadValidatingInterceptor() {
        var interceptor = new PayloadValidatingInterceptor();
        interceptor.setSchema(new ClassPathResource("eiam/nevisidm_servicetypes_v1_43.xsd"));
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        return interceptor;
    }

    @Bean
    public JeapSaajSoapMessageFactory getJeapSaajSoapMessageFactory() {
        return new JeapSaajSoapMessageFactory();
    }
}
