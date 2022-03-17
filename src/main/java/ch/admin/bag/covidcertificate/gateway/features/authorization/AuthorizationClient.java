package ch.admin.bag.covidcertificate.gateway.features.authorization;

import ch.admin.bag.covidcertificate.gateway.features.authorization.dto.FunctionsDefinitionDto;
import ch.admin.bag.covidcertificate.gateway.features.authorization.dto.RoleDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Profile("authorization")
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
            Set<String> grantedFunctions = this.getCurrent(rawRoles);
            return grantedFunctions.contains(function);
        }
    }

    /**
     * Returns all permitted functions by given roles at given service.
     * This permission is bound to time and may change during time.
     *
     * @param rawRoles the current roles of the user (either from eIAM or from Claim)
     * @return list of permitted functions
     */
    private Set<String> getCurrent(List<String> rawRoles) {
        Set<String> grantedFunctions = Collections.emptySet();
        // map the raw roles to the configured roles
        final Set<String> roles = rawRoles.stream()
                .map(role -> requireRoleMap().get(role))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (roles.isEmpty()) {
            log.info("no supported roles in '{}'", rawRoles);
        } else {
            // keep authorizations which are currently valid
            List<FunctionsDefinitionDto.Function> functionsByPointInTime =
                    filterByPointInTime(LocalDateTime.now(), requireDefinitionsFunctions());
            // identify the functions granted to this time by given roles
            grantedFunctions = functionsByPointInTime.stream()
                    .filter(function -> isGranted(roles, function))
                    .map(FunctionsDefinitionDto.Function::getIdentifier)
                    .collect(Collectors.toSet());
        }

        log.info("grants: " + grantedFunctions);
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
    private boolean isGranted(Set<String> roles, FunctionsDefinitionDto.Function function) {
        String mandatory = function.getMandatory();
        boolean mandatoryValid = mandatory == null || roles.contains(mandatory);
        List<String> oneOf = function.getOneOf();
        boolean oneOfValid = oneOf != null && !oneOf.isEmpty() && oneOf.stream().anyMatch(roles::contains);
        return (mandatoryValid && oneOfValid);
    }

    private List<FunctionsDefinitionDto.Function> filterByPointInTime(LocalDateTime pointInTime, List<FunctionsDefinitionDto.Function> functions) {
        List<FunctionsDefinitionDto.Function> result = Collections.emptyList();
        if (functions != null && pointInTime != null) {
            result = functions.stream()
                    .parallel()
                    .filter(function -> isBetween(pointInTime, function))
                    .collect(Collectors.toList());
        }
        return result;
    }

    private boolean isBetween(LocalDateTime pointInTime, FunctionsDefinitionDto.Function function) {
        boolean between = false;
        if (function != null) {
            boolean fromSmallerEquals = (function.getFrom() == null || function.getFrom().isBefore(pointInTime) || function.getFrom().isEqual(pointInTime));
            boolean untilLargerEquals = (function.getUntil() == null || function.getUntil().isAfter(pointInTime) || function.getUntil().isEqual(pointInTime));
            between = fromSmallerEquals && untilLargerEquals;
        }
        return between;
    }


    @Cacheable(AUTHORIZATION_DEFINITIONS_CACHE_NAME)
    public List<FunctionsDefinitionDto.Function> requireDefinitionsFunctions() {
        final var uri = UriComponentsBuilder.fromHttpUrl(managementServiceURL + authorizationApiV1Path + DEFINITION_RESOURCE_PATH + SERVICE_PATH_VARIABLE).toUriString();
        FunctionsDefinitionDto response = defaultWebClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(FunctionsDefinitionDto.class)
                .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null for request " + uri)))
                .block();

        return response.getFunctions();
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
        }
        return roleMapping;
    }

    @Scheduled(cron = "${cc-management-service.authorization.data-sync.cron}")
    @CacheEvict(value = {AUTHORIZATION_DEFINITIONS_CACHE_NAME, AUTHORIZATION_ROLEMAP_CACHE_NAME}, allEntries = true)
    public void fetchAndSaveAuthorizationData() {
        log.info("Reset of cache 'AUTHORIZATION_DEFINITIONS_CACHE_NAME' and 'AUTHORIZATION_ROLEMAP_CACHE_NAME'.");
    }
}
