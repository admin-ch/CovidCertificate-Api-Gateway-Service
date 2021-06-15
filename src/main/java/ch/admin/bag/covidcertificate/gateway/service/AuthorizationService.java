package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.DtoWithAuthorization;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {

    @Value("#{'${allowed-common-names}'.split(',')}")
    private List<String> allowedCommonNames;

    private final BearerTokenValidationService bearerTokenValidationService;
    private final IdentityAuthorizationClient identityAuthorizationClient;

    public String validateAndGetId(DtoWithAuthorization dtoWithAuthorization) throws InvalidBearerTokenException {
        var commonName = ((CustomHeaderAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
        if (allowedCommonNames.contains(commonName)) {
            if (dtoWithAuthorization.getIdentity() != null) {
                identityAuthorizationClient.authorize(dtoWithAuthorization.getIdentity().getUuid(), dtoWithAuthorization.getIdentity().getIdpSource());
                return dtoWithAuthorization.getIdentity().getUuid();
            }
            throw new InvalidBearerTokenException(INVALID_IDENTITY_USER);
        } else {
            return bearerTokenValidationService.validate(dtoWithAuthorization.getOtp());
        }
    }

}
