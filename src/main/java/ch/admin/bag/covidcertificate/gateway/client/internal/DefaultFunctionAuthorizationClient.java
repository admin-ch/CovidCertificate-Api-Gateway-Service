package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.FunctionAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.features.authorization.AuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

import static ch.admin.bag.covidcertificate.gateway.Constants.CLIENT_NAME_KEY;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class DefaultFunctionAuthorizationClient implements FunctionAuthorizationClient {

    private final AuthorizationClient authorizationClient;

    @Override
    public void validateUserAuthorization(UserAuthorizationData userAuthorizationData, Function function) {
        if (!authorizationClient.isAuthorized(userAuthorizationData.getRoles(), function.getIdentifier())) {
            throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
        }
    }
}
