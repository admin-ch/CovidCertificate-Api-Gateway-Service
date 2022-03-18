package ch.admin.bag.covidcertificate.gateway.client;

import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;

public interface IdentityAuthorizationClient {
    UserAuthorizationData fetchUserAndGetAuthData(String uuid, String idpSource);
}
