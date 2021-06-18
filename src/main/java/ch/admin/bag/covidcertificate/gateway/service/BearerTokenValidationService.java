package ch.admin.bag.covidcertificate.gateway.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;

@Component
@Slf4j
public class BearerTokenValidationService {

    private static final String COVID_CERT_CREATION = "covidcertcreation";
    private static final String SCOPE_CLAIM_KEY = "scope";
    private static final String USER_EXT_ID_CLAIM_KEY = "userExtId";
    private static final String IDP_SOURCE_CLAIM_KEY = "idpsource";
    private static final String TYP_CLAIM_KEY = "typ";
    private static final String AUTH_MACHINE_JWT = "authmachine+jwt";

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

    public String validate(String token) throws InvalidBearerTokenException {
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

            log.debug("Found Claims in JWT scope {}, userExtId {}, idpSource {}", scope, userExtId, idpSource);

            validateScope(scope);
            validateClaim(userExtId, USER_EXT_ID_CLAIM_KEY);
            validateClaim(idpSource, IDP_SOURCE_CLAIM_KEY);
            validateClaim(typ, AUTH_MACHINE_JWT);

            return userExtId;

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

}
