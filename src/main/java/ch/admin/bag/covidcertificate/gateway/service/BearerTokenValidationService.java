package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.Constants.IDP_SOURCE_CLAIM_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_CREATE_CERTIFICATE_TYPE;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_SYSTEM_API;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TIMESTAMP_KEY;
import static ch.admin.bag.covidcertificate.gateway.Constants.LOG_FORMAT;
import static ch.admin.bag.covidcertificate.gateway.Constants.SEC_KPI_EXT_ID;
import static ch.admin.bag.covidcertificate.gateway.Constants.SEC_KPI_IDP_SOURCE;
import static ch.admin.bag.covidcertificate.gateway.Constants.SEC_KPI_IP_ADDRESS;
import static ch.admin.bag.covidcertificate.gateway.Constants.SEC_KPI_OTP_JWT_ID;
import static ch.admin.bag.covidcertificate.gateway.Constants.SEC_KPI_OTP_TYPE;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_BEARER;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_OTP_LENGTH;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.MISSING_BEARER;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Slf4j
@RequiredArgsConstructor
public class BearerTokenValidationService {

    private static final String COVID_CERT_CREATION = "covidcertcreation";
    private static final String SCOPE_CLAIM_KEY = "scope";
    private static final String USER_EXT_ID_CLAIM_KEY = "userExtId";
    private static final String USER_ROLES_CLAIM_KEY = "userroles";
    private static final String TYP_CLAIM_KEY = "typ";
    private static final String AUTH_MACHINE_JWT = "authmachine+jwt";
    private static final String OTP_CLAIM_KEY = "otp";
    private final OtpRevocationService otpRevocationService;
    @Value("${cc-api-gateway-service.jwt.publicKey}")
    private String publicKey;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        final KeyFactory rsa = KeyFactory.getInstance("RSA");
        final X509EncodedKeySpec spec = new X509EncodedKeySpec(Decoders.BASE64.decode(publicKey));
        final Key signingKey;

        try {
            signingKey = rsa.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            log.error("Error during generate private key", e);
            throw new IllegalStateException(e);
        }

        jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();

    }

    public UserAuthorizationData validate(String token, String ipAddress) throws InvalidBearerTokenException {
        log.trace("validate token {}", token);

        if (token == null) {
            log.warn("Token is missing");
            throw new InvalidBearerTokenException(MISSING_BEARER);
        }

        if (!token.startsWith("eyJ")) {
            log.warn("Token has invalid start characters");
            throw new InvalidBearerTokenException(INVALID_OTP_LENGTH);
        }

        try {
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);

            String userExtId = claimsJws.getBody().get(USER_EXT_ID_CLAIM_KEY, String.class);
            String idpSource = claimsJws.getBody().get(IDP_SOURCE_CLAIM_KEY, String.class);
            String scope = claimsJws.getBody().get(SCOPE_CLAIM_KEY, String.class);
            String typ = claimsJws.getBody().get(TYP_CLAIM_KEY, String.class);
            var rolesArray = claimsJws.getBody().get(USER_ROLES_CLAIM_KEY, String[].class);

            List<String> roles = ArrayUtils.isEmpty(rolesArray) ? Collections.emptyList() : Arrays.asList(rolesArray);

            log.debug("Found Claims in JWT {}, {}, {}, {}",
                    kv(SCOPE_CLAIM_KEY, scope),
                    kv(USER_EXT_ID_CLAIM_KEY, userExtId),
                    kv(IDP_SOURCE_CLAIM_KEY, idpSource),
                    kv(USER_ROLES_CLAIM_KEY, roles));

            String jti = claimsJws.getBody().getId();
            if (isRevoked(jti)) {
                log.warn("Call with revoked otp with {}", kv("jti", jti));
                throw new InvalidBearerTokenException(INVALID_BEARER);
            }

            validateScope(scope);
            validateClaim(userExtId, USER_EXT_ID_CLAIM_KEY);
            validateClaim(idpSource, IDP_SOURCE_CLAIM_KEY);
            validateClaim(typ, AUTH_MACHINE_JWT);

            logSecKPI(ipAddress, claimsJws, userExtId, idpSource, jti);

            return new UserAuthorizationData(userExtId, idpSource, roles);

        } catch (ExpiredJwtException e) {
            log.warn("Token expired", e);
            throw new InvalidBearerTokenException(INVALID_BEARER);
        } catch (SignatureException e) {
            if (e.getMessage().toLowerCase().contains("signature length not correct")) {
                log.warn("Invalid signature length", e);
                throw new InvalidBearerTokenException(INVALID_OTP_LENGTH);
            } else {
                log.warn("Signature invalid", e);
                throw new InvalidBearerTokenException(INVALID_BEARER);
            }
        } catch (UnsupportedJwtException e) {
            log.warn("Token is not signed", e);
            throw new InvalidBearerTokenException(INVALID_BEARER);
        } catch (Exception e) {
            log.warn("Exception during validation of token", e);
            throw new InvalidBearerTokenException(INVALID_BEARER);
        }
    }

    private void validateScope(String scope) throws InvalidBearerTokenException {
        if (!StringUtils.hasText(scope) || !COVID_CERT_CREATION.equals(scope)) {
            log.warn("scope not present or invalid");
            throw new InvalidBearerTokenException(INVALID_BEARER);
        }
    }

    private void validateClaim(String claim, String text) throws InvalidBearerTokenException {
        if (!StringUtils.hasText(claim)) {
            log.warn("{} not present", text);
            throw new InvalidBearerTokenException(INVALID_BEARER);
        }
    }

    private boolean isRevoked(String jti) {
        return otpRevocationService.getOtpRevocations()
                .stream()
                .anyMatch(otpRevocation -> otpRevocation.getJti().equals(jti));
    }

    private void logSecKPI(String ipAddress, Jws<Claims> claimsJws, String userExtId, String idpSource, String jti) {
        log.info("sec-kpi: {} {} {} {} {} {} {}",
                kv(KPI_TIMESTAMP_KEY, LocalDateTime.now().format(LOG_FORMAT)),
                kv(KPI_CREATE_CERTIFICATE_TYPE, KPI_SYSTEM_API),
                kv(SEC_KPI_OTP_JWT_ID, jti),
                kv(SEC_KPI_OTP_TYPE, claimsJws.getBody().get(OTP_CLAIM_KEY, String.class)),
                kv(SEC_KPI_IP_ADDRESS, ipAddress),
                kv(SEC_KPI_EXT_ID, userExtId),
                kv(SEC_KPI_IDP_SOURCE, idpSource));
    }
}
