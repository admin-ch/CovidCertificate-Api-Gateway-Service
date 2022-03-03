package ch.admin.bag.covidcertificate.gateway.client;

import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;

public interface FunctionAuthorizationClient {
    void validateUserAuthorization(IdentityDto identity, Function function);
}
