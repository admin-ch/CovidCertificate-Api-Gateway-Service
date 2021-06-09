package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile(ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
public class MockIdentityAuthorizationClient implements IdentityAuthorizationClient {

    @Override
    public void authorize(String uuid, String idpSource) {
        log.info("Call the mock identity authorization");
    }
}
