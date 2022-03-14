package ch.admin.bag.covidcertificate.gateway.client;

import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;

public interface IdentityAuthorizationClient {
    UserAuthorizationData authorize(String uuid, String idpSource);
}
