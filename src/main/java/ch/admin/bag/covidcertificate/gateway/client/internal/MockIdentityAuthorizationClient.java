package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Authorization;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ProfileState;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Role;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.gateway.Constants.CLIENT_NAME_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.IDP_SOURCE_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.UUID_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@Profile(ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
public class MockIdentityAuthorizationClient implements IdentityAuthorizationClient {
    private static final String ROLE_CREATOR = "9500.GGG-Covidcertificate.CertificateCreator";
    private static final String ROLE_SUPERUSER = "9500.GGG-Covidcertificate.SuperUserCC";

    @Override
    public UserAuthorizationData authorize(String uuid, String idpSource) {
        log.info("Call the mock identity authorization");

        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(idpSource)) {
            log.info("User not valid {} {}",
                    kv(UUID_CLAIM_KEY, uuid),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource));
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        } else {
            log.trace("User info is valid");
        }

        QueryUsersResponse queryUsersResponse = null;
        try {
            queryUsersResponse = queryUser("test_profile_authorized_first_active_role.xml");
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (checkUserExists(queryUsersResponse)) {
            log.info("User does not exist in eIAM. {} {} {}",
                    kv(UUID_CLAIM_KEY, uuid),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource),
                    kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        } else {
            log.trace("User exists");
        }

        if (!hasUserRoleSuperUserOrCreator(queryUsersResponse)) {
            log.info("User does not have required role in eIAM. {} {} {}",
                    kv(UUID_CLAIM_KEY, uuid),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource),
                    kv(CLIENT_NAME_KEY, EIAMConfig.CLIENT_NAME));
            throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
        } else {
            log.trace("User has right roles");
        }
        log.trace("Authorization checked successfully.");


        List<String> roles = queryUsersResponse.getReturns().stream()                           // QueryUsersResponse -> Stream<User>
                .map(User::getProfiles)                                                         // Stream<User> -> Stream<List<Profile>>
                .flatMap(List::stream)                                                          // Stream<List<Profile>> -> Stream<Profile>
                .filter(profile -> ProfileState.ACTIVE == profile.getState())                   // Stream<Profile> -> Stream<Profile.Active>
                .map(ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Profile::getRoles) // Stream<Profile.Active> -> Stream<List<Role>>
                .flatMap(List::stream)                                                          // Stream<List<Role>> -> Stream<Role>
                .map(Role::getName)                                                             // Stream<Role> -> Stream<String>
                .collect(Collectors.toList());                                                  // Stream<Role> -> List<String>

        return new UserAuthorizationData(uuid, idpSource, roles);
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
            return response.getReturns().stream().anyMatch(
                    user -> user.getProfiles().stream().anyMatch(
                            profile -> profile.getState().equals(ProfileState.ACTIVE) &&
                                    profile.getAuthorizations().stream().anyMatch(this::isRoleSuperUserOrCreator)));
        } catch (Exception e) {
            log.error("Error when checking eIAM user role exists.", e);
            throw e;
        }
    }

    private boolean isRoleSuperUserOrCreator(Authorization authorization) {
        return authorization.getRole().getExtId().equals(ROLE_CREATOR) ||
                authorization.getRole().getExtId().equals(ROLE_SUPERUSER);
    }

    private QueryUsersResponse queryUser(String fileName) throws JAXBException, IOException {
        Resource resourceFile = new ClassPathResource(fileName);
        JAXBContext context = JAXBContext.newInstance(QueryUsersResponse.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (QueryUsersResponse) unmarshaller.unmarshal(resourceFile.getInputStream());
    }
}
