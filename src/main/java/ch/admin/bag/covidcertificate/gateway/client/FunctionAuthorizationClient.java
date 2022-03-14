package ch.admin.bag.covidcertificate.gateway.client;

import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;

public interface FunctionAuthorizationClient {
    void validateUserAuthorization(UserAuthorizationData userAuthorizationData, Function function);
}
