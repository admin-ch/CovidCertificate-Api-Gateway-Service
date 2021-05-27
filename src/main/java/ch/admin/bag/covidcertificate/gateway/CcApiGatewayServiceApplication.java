package ch.admin.bag.covidcertificate.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Slf4j
@SpringBootApplication
public class CcApiGatewayServiceApplication {

    public static void main(String[] args) {

        String filePath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("truststore.jks")).getFile();
        System.setProperty("javax.net.ssl.trustStore", filePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        Environment env = SpringApplication.run(CcApiGatewayServiceApplication.class, args).getEnvironment();

        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                        "Yeah!!! {} is running! \n\t" +
                        "\n" +
                        "\tSwaggerUI: \t{}://localhost:{}/swagger-ui.html\n\t" +
                        "Profile(s): \t{}" +
                        "\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                env.getActiveProfiles());

    }
}
