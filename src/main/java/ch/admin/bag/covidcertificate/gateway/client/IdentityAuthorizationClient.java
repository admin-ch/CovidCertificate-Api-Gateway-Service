package ch.admin.bag.covidcertificate.gateway.client;

public interface IdentityAuthorizationClient {
    void authorize(String uuid, String idpSource);
}
