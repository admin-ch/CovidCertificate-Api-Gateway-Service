package ch.admin.bag.covidcertificate.gateway.client.eiam;

import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryClientsResponse;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static ch.admin.bag.covidcertificate.gateway.Constants.CLIENT_NAME_KEY;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Component("eIAMAdminService")
@Slf4j
@Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class EIAMHealthIndicator implements HealthIndicator {
    private final EIAMClient eiamClient;

    @Override
    public Health health() {
        log.info("Calling eIAM AdminService queryClients. {}", kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));
        try {
            QueryClientsResponse response = eiamClient.queryClient(EIAMConfig.CLIENT_NAME);
            var clients = response.getReturns();
            if (clients == null || clients.isEmpty()) {
                log.info("Client does not exist in eIAM. {}", kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));
                return Health.down().build();
            } else if (clients.get(0).getName().equals(EIAMConfig.CLIENT_NAME)) {
                log.info("Client does exist in eIAM. {}", kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));
                return Health.up().build();
            }
            return Health.down().build();
        } catch (Exception e) {
            log.error("Error when calling eIAM AdminService queryClients.", e);
            return Health.down(e).build();
        }
    }
}
