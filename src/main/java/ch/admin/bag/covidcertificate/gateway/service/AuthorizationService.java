package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.client.FunctionAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Authorization;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Profile;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ProfileState;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Role;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.features.authorization.AuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.DtoWithAuthorization;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {

    private final BearerTokenValidationService bearerTokenValidationService;
    private final IdentityAuthorizationClient identityAuthorizationClient;
    private final FunctionAuthorizationClient functionAuthorizationClient;


    @Value("#{'${allowed-common-names-for-identity}'.split(',')}")
    private List<String> allowedCommonNamesForIdentity;

    public String validateAndGetId(@NotNull DtoWithAuthorization dtoWithAuthorization, String ipAddress, Function function) throws InvalidBearerTokenException {

        IdentityDto identity = null;

        if (Strings.isNotBlank(dtoWithAuthorization.getOtp())) {
            log.trace("Checking access via token.");
            identity = bearerTokenValidationService.validate(dtoWithAuthorization.getOtp(), ipAddress);
        } else if (!Objects.isNull(dtoWithAuthorization.getIdentity())) {
            log.trace("Checking access via identity.");
            var commonName = ((CustomHeaderAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();

            if (!allowedCommonNamesForIdentity.contains(commonName))
                throw new CreateCertificateException(INVALID_IDENTITY_USER);

            log.trace("Common name is part of configured list");
            log.trace("...checking authorisation");
            identityAuthorizationClient.authorize(dtoWithAuthorization.getIdentity().getUuid(), dtoWithAuthorization.getIdentity().getIdpSource());
            identity = dtoWithAuthorization.getIdentity();
        } else {
            log.error("No OTP nor Identity is present in the request.");
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        }

        functionAuthorizationClient.validateUserAuthorization(identity, function);

        return identity.getUuid();
    }
}