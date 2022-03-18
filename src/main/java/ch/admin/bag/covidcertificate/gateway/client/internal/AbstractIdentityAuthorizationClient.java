package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Profile;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ProfileState;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Role;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.gateway.Constants.IDP_SOURCE_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.UUID_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
public abstract class AbstractIdentityAuthorizationClient implements IdentityAuthorizationClient {

    protected abstract User queryUser(String uuid, String idpSource);

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
}
