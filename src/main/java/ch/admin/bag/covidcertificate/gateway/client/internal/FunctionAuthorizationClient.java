package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.features.authorization.AuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class FunctionAuthorizationClient {

    private final AuthorizationClient authorizationClient;

    public void validateUserAuthorization(UserAuthorizationData userAuthorizationData, Function function) {

        if (authorizationClient.isAuthorized(userAuthorizationData.getRoles(), function.getIdentifier())) return;

        throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
    }
}
