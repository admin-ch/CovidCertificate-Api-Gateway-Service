package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Profile;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ProfileState;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Role;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.gateway.Constants.CLIENT_NAME_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.IDP_SOURCE_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.UUID_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@org.springframework.context.annotation.Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class DefaultIdentityAuthorizationClient implements IdentityAuthorizationClient {

    private final EIAMClient eiamClient;

    @Override
    public UserAuthorizationData fetchUserAndGetAuthData(String uuid, String idpSource) {
        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(idpSource)) {
            log.info("User not valid {} {}",
                    kv(UUID_CLAIM_KEY, uuid),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource));
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        } else {
            log.trace("User info is valid");
        }

        User user = queryUser(uuid, idpSource);

        Predicate<Profile> isActiveProfilePredicate = profile -> ProfileState.ACTIVE == profile.getState();
        Predicate<Profile> isDefaultProfilePredicate = Profile::isDefaultProfile;

        List<String> roles = user.getProfiles()
                .stream()
                .filter(isDefaultProfilePredicate.and(isActiveProfilePredicate))
                .map(Profile::getRoles)
                .flatMap(Collection::stream)
                .map(Role::getExtId)
                .distinct()
                .collect(Collectors.toList());

        log.trace("Authorization checked successfully.");
        return new UserAuthorizationData(uuid, idpSource, roles);
    }


    private User queryUser(String uuid, String idpSource) {
        try {
            log.info("Calling eIAM AdminService queryUsers. {} {} {}",
                    kv(UUID_CLAIM_KEY, uuid),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource),
                    kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));

            Optional<User> eiamUser = eiamClient.queryUser(uuid, idpSource, EIAMConfig.CLIENT_NAME)
                    .getReturns()
                    .stream()
                    .findFirst();

            if (eiamUser.isEmpty()) {
                log.info("User does not exist in eIAM. {} {} {}",
                        kv(UUID_CLAIM_KEY, uuid),
                        kv(IDP_SOURCE_CLAIM_KEY, idpSource),
                        kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));
                throw new CreateCertificateException(INVALID_IDENTITY_USER);
            }

            log.info("User has been found in eIAM. {} {} {}",
                    kv(UUID_CLAIM_KEY, uuid),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource),
                    kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));

            return eiamUser.get();


        } catch (Exception e) {
            log.error("Error when calling eIAM AdminService queryUsers. {} {} {}",
                    kv(UUID_CLAIM_KEY, uuid),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource),
                    kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME), e);
            throw e;
        }
    }

}
