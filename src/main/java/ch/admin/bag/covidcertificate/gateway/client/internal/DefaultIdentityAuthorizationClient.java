package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
public class DefaultIdentityAuthorizationClient implements IdentityAuthorizationClient {

    @Override
    public void authorize(String uuid, String idpSource) {
        // ToDo Authentication to eIAM
    }
}
