package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.FunctionAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.features.authorization.AuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
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

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;

@Service
@Slf4j
@Profile(ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class MockFunctionAuthorizationClient implements FunctionAuthorizationClient {

    private final AuthorizationClient authorizationClient;

    @Override
    public void validateUserAuthorization(UserAuthorizationData userAuthorizationData, Function function) {
        if (!authorizationClient.isAuthorized(userAuthorizationData.getRoles(), function.getIdentifier())) {
            throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
        }
    }
}
