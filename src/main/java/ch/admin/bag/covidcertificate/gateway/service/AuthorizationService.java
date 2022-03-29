package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.internal.FunctionAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.DtoWithAuthorization;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;

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

        UserAuthorizationData userAuthorizationData;

        String otp = dtoWithAuthorization.getOtp();
        if (isIdentifiedWithOTP(otp)) {
            log.info("Checking access via 'otp'/'ltotp'.");
            userAuthorizationData = bearerTokenValidationService.validateOtpAndGetAuthData(otp, ipAddress);
        } else {
            IdentityDto identity = dtoWithAuthorization.getIdentity();
            if (isIdentifiedWithIdentity(identity)) {
                log.info("Checking access via 'identity'.");
                userAuthorizationData = identityAuthorizationClient.fetchUserAndGetAuthData(identity.getUuid(), identity.getIdpSource());
            } else {
                log.error("No OTP nor Identity is present in the request.");
                throw new CreateCertificateException(INVALID_IDENTITY_USER);
            }
        }

        if (!userAuthorizationData.isValid()) {
            log.info("UserAuthorizationData is not valid");
            throw new CreateCertificateException(INVALID_IDENTITY_USER);
        }

        functionAuthorizationClient.validateUserAuthorization(userAuthorizationData, function);
        return userAuthorizationData.getUserId();
    }

    private boolean isIdentifiedWithOTP(String otp) {
        return Strings.isNotBlank(otp);
    }

    private boolean isIdentifiedWithIdentity(IdentityDto identity) {

        boolean result = false;

        if (!Objects.isNull(identity)) {
            var commonName = ((CustomHeaderAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
            result = allowedCommonNamesForIdentity.contains(commonName);
        }

        return result;
    }
}