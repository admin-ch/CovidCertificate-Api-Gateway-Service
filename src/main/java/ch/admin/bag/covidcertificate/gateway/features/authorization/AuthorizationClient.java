package ch.admin.bag.covidcertificate.gateway.features.authorization;

import ch.admin.bag.covidcertificate.gateway.features.authorization.dto.RoleDataDto;
import ch.admin.bag.covidcertificate.gateway.features.authorization.dto.ServiceData;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.nimbusds.oauth2.sdk.util.MapUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationClient {

    private static final String AUTHORIZATION_ROLEMAP_CACHE_NAME = "AUTHORIZATION_ROLEMAP_CACHE_NAME";
    private static final String DEFINITION_RESOURCE_PATH = "definition/";
    private static final String SERVICE_PATH_VARIABLE = "api-gateway";

    private static final String AUTHORIZATION_DEFINITIONS_CACHE_NAME = "AUTHORIZATION_DEFINITIONS_CACHE_NAME";
    private static final String ROLE_MAPPING_RESOURCE_PATH = "role-mapping";

    private final WebClient defaultWebClient;

    @Value("${cc-management-service.uri}")
    private String managementServiceURL;

    @Value("${cc-management-service.authorization.api.v1-path}")
    private String authorizationApiV1Path;

    @PostConstruct
    private void init() {
        this.fetchAndSaveAuthorizationData();
    }

    public boolean isAuthorized(List<String> rawRoles, String function) {
        // OTP/LTOTP pre-migration Authorization-Concept:
        // If the list is empty, it is deduced that the
        // roles have not been encoded in the OTP/LTOTP,
        // so all authorizations are granted.
        if (CollectionUtils.isEmpty(rawRoles)) {
            return true;
        } else {
            Set<String> grantedFunctions = this.getCurrentGrantedFunctions(rawRoles);
            return grantedFunctions.contains(function);
        }
    }

    /**
     * Returns all permitted functions for a given roles set at current instant time.
     *
     * @param rawRoles the current roles of the user (either from eIAM or from Claim)
     * @return list of permitted functions
     */
    public Set<String> getCurrentGrantedFunctions(List<String> rawRoles) {

        ServiceData serviceData = requireFunctionsDefinitions();
        if (serviceData == null || serviceData.getFunctions().isEmpty()) {
            log.info("Functions definitions are null or empty, no function is granted.");
            return Collections.emptySet();
        }

        SortedMap<String, String> roleMap = requireRoleMap();
        if (MapUtils.isEmpty(roleMap)) {
            log.info("RoleMap is null or empty, no function is granted.");
            return Collections.emptySet();
        }

        // Map user roles names to the claim names
        final Set<String> roles = rawRoles.stream()
                .map(roleMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(roles)) {
            log.info("No supported roles found in RoleMap for '{}'", rawRoles);
            return Collections.emptySet();
        }

        // Filter functions which are available at current instant time
        var functions = serviceData.getFunctions();
        List<ServiceData.Function> functionsByPointInTime = filterByPointInTime(LocalDateTime.now(), functions.values());

        // Filter functions which are available for mapped roles set
        Set<String> grantedFunctions = functionsByPointInTime.stream()
                .filter(function -> isGranted(roles, function))
                .map(ServiceData.Function::getIdentifier)
                .collect(Collectors.toSet());

        log.info("Role set {} has granted functions: {}", roles, grantedFunctions);

        return grantedFunctions;
    }

    /**
     * Returns <code>true</code> for given function IF:
     * <ul>
     *     <li>mandatory</li>
     *     is valid when either is <code>null</code> or the given role is part of the user's roles
     *     <li>one-of</li>
     *     is valid when either is <code>null</code> or one of the given roles is part of the user's roles
     * </ul>
     * <li>
     * The given function is only permitted when both conditions are valid.
     *
     * @param roles    the user's roles
     * @param function the function to check
     * @return <code>true</code> only if both mandatory and one-of are valid
     */
    private boolean isGranted(Set<String> roles, ServiceData.Function function) {
        boolean allAdditionalValid = true;
        if (CollectionUtils.isNotEmpty(function.getAdditional())) {
            // check additional functions which are currently valid
            List<ServiceData.Function> activeAdditionalFunctions =
                    filterByPointInTime(LocalDateTime.now(), function.getAdditional());

            //TODO: fix infinite recursion
            allAdditionalValid = activeAdditionalFunctions.stream().allMatch(func -> isGranted(roles, func));
        }
        List<String> oneOf = function.getOneOf();
        if (CollectionUtils.isEmpty(oneOf)) {
            return allAdditionalValid;
        }
        boolean oneOfValid = oneOf.stream().anyMatch(roles::contains);
        return (allAdditionalValid && oneOfValid);
    }

    private List<ServiceData.Function> filterByPointInTime(LocalDateTime pointInTime, Collection<ServiceData.Function> functions) {
        List<ServiceData.Function> result = Collections.emptyList();
        if (functions != null && pointInTime != null) {
            result = functions.stream()
                    .parallel()
                    .filter(function -> function.isBetween(pointInTime))
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Cacheable(AUTHORIZATION_DEFINITIONS_CACHE_NAME)
    public ServiceData requireFunctionsDefinitions() {
        final var uri = UriComponentsBuilder.fromHttpUrl(managementServiceURL + authorizationApiV1Path + DEFINITION_RESOURCE_PATH + SERVICE_PATH_VARIABLE).toUriString();
        return defaultWebClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ServiceData.class)
                .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null for request " + uri)))
                .block();
    }

    @Cacheable(AUTHORIZATION_ROLEMAP_CACHE_NAME)
    public SortedMap<String, String> requireRoleMap() {
        log.info("Fetch Role-Map from management-service.");
        final var uri = UriComponentsBuilder.fromHttpUrl(managementServiceURL + authorizationApiV1Path + ROLE_MAPPING_RESOURCE_PATH).toUriString();
        RoleDataDto[] roleMap = defaultWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(RoleDataDto[].class)
                .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null for request " + uri)))
                .block();

        TreeMap<String, String> roleMapping = new TreeMap<>();
        assert roleMap != null;
        for (RoleDataDto roleDataDto : roleMap) {
            roleMapping.put(roleDataDto.getClaim(), roleDataDto.getIntern());
            roleMapping.put(roleDataDto.getEiam(), roleDataDto.getIntern());
        }
        return roleMapping;
    }

    @Scheduled(cron = "${cc-management-service.authorization.data-sync.cron}")
    @CacheEvict(value = {AUTHORIZATION_DEFINITIONS_CACHE_NAME, AUTHORIZATION_ROLEMAP_CACHE_NAME}, allEntries = true)
    public void fetchAndSaveAuthorizationData() {
        log.info("Reset of cache 'AUTHORIZATION_DEFINITIONS_CACHE_NAME' and 'AUTHORIZATION_ROLEMAP_CACHE_NAME'.");
    }
}
