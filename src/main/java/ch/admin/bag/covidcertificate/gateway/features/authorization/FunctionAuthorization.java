package ch.admin.bag.covidcertificate.gateway.features.authorization;

import ch.admin.bag.covidcertificate.authorization.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class FunctionAuthorization {

    private final AuthorizationService authorizationService;

    public void validateUserAuthorization(UserAuthorizationData userAuthorizationData, Function function) {

        Set<String> grantedFunctions = authorizationService.getCurrent(
                AuthorizationService.SERVICE_API_GATEWAY, userAuthorizationData.getRoles());

        if (grantedFunctions.contains(function.getIdentifier())) return;

        throw new CreateCertificateException(INVALID_IDENTITY_USER_ROLE);
    }
}
