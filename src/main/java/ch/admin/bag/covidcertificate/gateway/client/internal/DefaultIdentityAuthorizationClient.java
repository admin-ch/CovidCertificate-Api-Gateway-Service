package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.generated.Authorization;
import ch.admin.bag.covidcertificate.gateway.client.eiam.generated.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class DefaultIdentityAuthorizationClient implements IdentityAuthorizationClient {
    private static final String ROLE = "9500.GGG-Covidcertificate.CertificateCreator";

    public final EIAMClient eiamClient;

    @Override
    public void authorize(String uuid, String idpSource) {
        QueryUsersResponse response = queryUser(uuid, idpSource);
        if (checkUserExists(response)) {
            log.info("User does not exist in eIAM. {} {}", kv("uuid", uuid), kv("idpSource", idpSource));
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        }
        if (checkUserRoleExists(response)) {
            log.info("User does not have required role in eIAM. {} {}", kv("uuid", uuid), kv("idpSource", idpSource));
            throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
        }
    }

    private QueryUsersResponse queryUser(String uuid, String idpSource) {
        try {
            return eiamClient.queryUser(uuid, idpSource);
        } catch (Exception e) {
            log.error("Error when calling eIAM-AM queryUser service.", e);
            throw e;
        }
    }

    private Boolean checkUserExists(QueryUsersResponse response) {
        try {
            return (response.getReturn() == null || response.getReturn().isEmpty());
        } catch (Exception e) {
            log.error("Error when checking eIAM-AM user exists.", e);
            throw e;
        }
    }

    private Boolean checkUserRoleExists(QueryUsersResponse response) {
        try {
            List<Authorization> authorizations = response.getReturn().get(0)
                    .getProfiles().get(0)
                    .getAuthorizations();
            return (authorizations.stream().anyMatch(authorization -> authorization.getRole().getExtId().equals(ROLE)));
        } catch (Exception e) {
            log.error("Error when checking eIAM-AM user role exists.", e);
            throw e;
        }
    }
}
