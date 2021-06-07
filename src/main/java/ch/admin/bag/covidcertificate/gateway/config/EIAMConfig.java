package ch.admin.bag.covidcertificate.gateway.config;

import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class EIAMConfig {
    private static final String CONTEXT_PATH = "ch.admin.bag.covidcertificate.gateway.client.eiam.generated";
    @Value("${eiam-am-service.url}")
    private String url;

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(CONTEXT_PATH);
        return marshaller;
    }

    @Bean
    public EIAMClient eIAMClient(Jaxb2Marshaller marshaller) {
        EIAMClient client = new EIAMClient();
        client.setDefaultUri(url);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}
