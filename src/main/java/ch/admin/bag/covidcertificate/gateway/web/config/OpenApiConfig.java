package ch.admin.bag.covidcertificate.gateway.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Covid Certificate API Gateway Service")
                        .description("Rest API for Covid Certificate API Gateway Service.")
                        .version("2.0.0")
                        .license(new License().name("Apache 2.0"))
                ).servers(List.of(
                        new Server().description("prod").url("https://ws.covidcertificate.bag.admin.ch"),
                        new Server().description("test").url("https://ws.covidcertificate-a.bag.admin.ch")));
    }
}
