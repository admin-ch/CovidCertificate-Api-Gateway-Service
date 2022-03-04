package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.FunctionAuthorizationClient;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;

@Service
@Slf4j
@Profile(ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class MockFunctionAuthorizationClient implements FunctionAuthorizationClient {

    private final AuthorizationClient authorizationClient;
    private final Map<IdentityDto, QueryUsersResponse> usersCache = new HashMap<>();

    @Override
    public void validateUserAuthorization(IdentityDto identity, Function function) {
        try {
            QueryUsersResponse queryUsersResponse;

            if (usersCache.containsKey(identity)) {
                queryUsersResponse = usersCache.get(identity);
            } else {
                queryUsersResponse = retrieveUserFromEIAM("test_profile_uuid_hans.xml");
                usersCache.put(identity, queryUsersResponse);
            }

            for (User user : queryUsersResponse.getReturns()) {
                for (ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Profile profile : user.getProfiles()) {
                    if (ProfileState.ACTIVE != profile.getState()) continue;

                    List<String> roles = profile.getAuthorizations().stream()
                            .map(Authorization::getRole)
                            .collect(Collectors.toList()).stream()
                            .map(Role::getExtId)
                            .collect(Collectors.toList());
                    if (authorizationClient.isAuthorized(roles, function.getIdentifier())) {
                        return;
                    }
                }

            }
            throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
        } catch (Exception e) {
            throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
        }
    }

    private QueryUsersResponse retrieveUserFromEIAM(String fileName) throws JAXBException, IOException {
        Resource resourceFile = new ClassPathResource(fileName);
        JAXBContext context = JAXBContext.newInstance(QueryUsersResponse.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (QueryUsersResponse) unmarshaller.unmarshal(resourceFile.getInputStream());
    }
}
