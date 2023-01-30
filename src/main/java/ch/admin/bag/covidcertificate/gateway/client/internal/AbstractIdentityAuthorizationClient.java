package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Profile;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ProfileState;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Role;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static ch.admin.bag.covidcertificate.gateway.Constants.IDP_SOURCE_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.UUID_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
public abstract class AbstractIdentityAuthorizationClient implements IdentityAuthorizationClient {

    private static final String USER_AUTHORIZATION_DATA_CACHE = "USER_AUTHORIZATION_DATA_CACHE";

    protected abstract User searchUser(String uuid, String idpSource);

    @Cacheable(USER_AUTHORIZATION_DATA_CACHE)
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

        User user = searchUser(uuid, idpSource);

        Predicate<Profile> isActiveProfilePredicate = profile -> ProfileState.ACTIVE == profile.getState();
        Predicate<Profile> isDefaultProfilePredicate = Profile::isDefaultProfile;

        List<String> roles = user.getProfiles()
                .stream()
                .filter(isDefaultProfilePredicate.and(isActiveProfilePredicate))
                .map(Profile::getRoles)
                .flatMap(Collection::stream)
                .map(Role::getExtId)
                .distinct().toList();

        log.trace("Authorization checked successfully.");
        return new UserAuthorizationData(uuid, idpSource, roles);
    }

    @Scheduled(fixedRateString = "${cc-api-gateway-service.cache-duration}")
    @CacheEvict(value = USER_AUTHORIZATION_DATA_CACHE, allEntries = true)
    public void cleanUserAuthorizationDataCache() {
        log.info("Cleaning cache of user autorization data.");
    }
}
