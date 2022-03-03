package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.FunctionAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Authorization;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ProfileState;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Role;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.features.authorization.AuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class DefaultFunctionAuthorizationClient implements FunctionAuthorizationClient {

    private final EIAMClient eiamClient;
    private final AuthorizationClient authorizationClient;

    private final Map<IdentityDto, QueryUsersResponse> usersCache = Collections.emptyMap();

    @Override
    public void validateUserAuthorization(IdentityDto identity, Function function) {

        QueryUsersResponse queryUsersResponse;

        if (usersCache.containsKey(identity)) {
            queryUsersResponse = usersCache.get(identity);
        } else {
            queryUsersResponse = retrieveUserFromEiam(identity);
            usersCache.put(identity, queryUsersResponse);
        }


        for (User user : queryUsersResponse.getReturns()) {
            for (ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Profile profile : user.getProfiles()) {
                if (ProfileState.ACTIVE != profile.getState()) continue;

                List<String> roles = profile.getAuthorizations().stream()
                        .map(Authorization::getRole)
                        .collect(Collectors.toList()).stream()
                        .map(Role::getName)
                        .collect(Collectors.toList());
                if (authorizationClient.isAuthorized(roles, function.getIdentifier())) {
                    return;
                }
            }

        }
        throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
    }

    private QueryUsersResponse retrieveUserFromEiam(IdentityDto identity) {
        try {
            log.info("Calling eIAM AdminService queryUsers. {} {} {}", kv("identity.getUuid()", identity.getUuid()), kv("idpSource", identity.getIdpSource()), kv("clientName", EIAMConfig.CLIENT_NAME));
            return eiamClient.queryUser(identity.getUuid(), identity.getIdpSource(), EIAMConfig.CLIENT_NAME);
        } catch (Exception e) {
            log.error("Error when calling eIAM AdminService queryUsers. {} {} {}", kv("identity.getUuid()", identity.getUuid()), kv("idpSource", identity.getIdpSource()), kv("clientName", EIAMConfig.CLIENT_NAME), e);
            throw e;
        }
    }
}
