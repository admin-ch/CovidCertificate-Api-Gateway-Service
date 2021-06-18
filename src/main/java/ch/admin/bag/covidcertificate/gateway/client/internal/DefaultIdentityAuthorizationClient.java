package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Authorization;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private static final String ROLE_SUPERUSER = "9500.GGG-Covidcertificate.SuperUserCC";

    private final EIAMClient eiamClient;

    @Override
    public void authorize(String uuid, String idpSource) {
        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(idpSource)) {
            log.info("User not valid {} {}", kv("uuid", uuid), kv("idpSource", idpSource));
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        }

        QueryUsersResponse response = queryUser(uuid, idpSource);
        if (checkUserExists(response)) {
            log.info("User does not exist in eIAM. {} {} {}", kv("uuid", uuid), kv("idpSource", idpSource), kv("clientName", EIAMConfig.CLIENT_NAME));
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        }
        if (!checkUserRoleExists(response)) {
            log.info("User does not have required role in eIAM. {} {} {}", kv("uuid", uuid), kv("idpSource", idpSource), kv("clientName", EIAMConfig.CLIENT_NAME));
            throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
        }
    }

    private QueryUsersResponse queryUser(String uuid, String idpSource) {
        try {
            log.info("Calling eIAM AdminService queryUsers. {} {} {}", kv("uuid", uuid), kv("idpSource", idpSource), kv("clientName", EIAMConfig.CLIENT_NAME));
            return eiamClient.queryUser(uuid, idpSource, EIAMConfig.CLIENT_NAME);
        } catch (Exception e) {
            log.error("Error when calling eIAM AdminService queryUsers. {} {} {}", kv("uuid", uuid), kv("idpSource", idpSource), kv("clientName", EIAMConfig.CLIENT_NAME), e);
            throw e;
        }
    }

    private boolean checkUserExists(QueryUsersResponse response) {
        try {
            return (response.getReturns() == null || response.getReturns().isEmpty());
        } catch (Exception e) {
            log.error("Error when checking eIAM user exists.", e);
            throw e;
        }
    }

    private boolean checkUserRoleExists(QueryUsersResponse response) {
        try {
            List<Authorization> authorizations = response.getReturns().get(0)
                    .getProfiles().get(0)
                    .getAuthorizations();
            return (authorizations.stream().anyMatch(authorization ->
                    authorization.getRole().getExtId().equals(ROLE) || authorization.getRole().getExtId().equals(ROLE_SUPERUSER)));
        } catch (Exception e) {
            log.error("Error when checking eIAM user role exists.", e);
            throw e;
        }
    }
}
