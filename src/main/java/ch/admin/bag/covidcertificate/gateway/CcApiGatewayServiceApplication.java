package ch.admin.bag.covidcertificate.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CcApiGatewayServiceApplication {

    public static void main(String[] args) {

        Environment env = SpringApplication.run(CcApiGatewayServiceApplication.class, args).getEnvironment();

        String truststorePassword = env.getProperty("cc-api-gateway-service.truststore.password");
        if (StringUtils.hasText(truststorePassword)) {
            String filePath = Objects.requireNonNull(Thread.currentThread()
                    .getContextClassLoader().getResource("truststore.jks")).getFile();
            System.setProperty("javax.net.ssl.trustStore", filePath);
            System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
            log.info("Custom truststore initialized");
        } else {
            log.info("No custom truststore initialized");
        }

        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        final String message = """
                ----------------------------------------------------------
                    "Yeah!!! {} is running!
                    
                    SwaggerUI:   {}://localhost:{}/swagger-ui.html
                    "Profile(s): {}
                ----------------------------------------------------------
                """;
        log.info(message,
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                env.getActiveProfiles());
    }
}
