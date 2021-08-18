package ch.admin.bag.covidcertificate.gateway;

import ch.admin.bag.covidcertificate.rest.tracing.TracerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackendLoggingConfig {

    @Bean
    public TracerConfiguration getTracerConfiguration() {
        return new TracerConfiguration();
    }
}
