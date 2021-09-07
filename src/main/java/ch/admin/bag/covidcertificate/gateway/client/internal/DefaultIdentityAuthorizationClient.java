package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Authorization;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ProfileState;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class DefaultIdentityAuthorizationClient implements IdentityAuthorizationClient {
    private static final String ROLE_CREATOR = "9500.GGG-Covidcertificate.CertificateCreator";
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
        if (!hasUserRoleSuperUserOrCreator(response)) {
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

    protected boolean hasUserRoleSuperUserOrCreator(QueryUsersResponse response) {
        try {
            AtomicBoolean result = new AtomicBoolean(false);
            response.getReturns().forEach(
                    user -> user.getProfiles().stream().filter(
                            profile -> profile.getState().equals(ProfileState.ACTIVE)).forEach(
                                    profile -> result.set(profile.getAuthorizations().stream().anyMatch(
                            this::isRoleSuperUserOrCreator))));
            return result.get();
        } catch (Exception e) {
            log.error("Error when checking eIAM user role exists.", e);
            throw e;
        }
    }

    private boolean isRoleSuperUserOrCreator(Authorization authorization) {
        return authorization.getRole().getExtId().equals(ROLE_CREATOR) ||
                authorization.getRole().getExtId().equals(ROLE_SUPERUSER);
    }
}
