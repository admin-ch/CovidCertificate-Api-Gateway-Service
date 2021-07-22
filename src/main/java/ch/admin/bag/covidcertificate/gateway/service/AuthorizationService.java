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

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {

    private final BearerTokenValidationService bearerTokenValidationService;
    private final IdentityAuthorizationClient identityAuthorizationClient;
    @Value("#{'${allowed-common-names-for-identity}'.split(',')}")
    private List<String> allowedCommonNamesForIdentity;

    public String validateAndGetId(DtoWithAuthorization dtoWithAuthorization, String ipAddress) throws InvalidBearerTokenException {
        var commonName = ((CustomHeaderAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
        if (allowedCommonNamesForIdentity.contains(commonName)) {
            if (dtoWithAuthorization.getIdentity() != null) {
                identityAuthorizationClient.authorize(dtoWithAuthorization.getIdentity().getUuid(), dtoWithAuthorization.getIdentity().getIdpSource());
                return dtoWithAuthorization.getIdentity().getUuid();
            }
        }
        return bearerTokenValidationService.validate(dtoWithAuthorization.getOtp(), ipAddress);
    }

}
