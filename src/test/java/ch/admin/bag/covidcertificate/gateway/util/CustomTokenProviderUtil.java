package ch.admin.bag.covidcertificate.gateway.util;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class CustomTokenProviderUtil {

    private static final String COVID_CERT_CREATION = "covidcertcreation";
    private static String SCOPE_CLAIM_KEY = "scope";
    private static final String USER_EXT_ID_CLAIM_KEY = "userExtId";
    private static final String IDP_SOURCE_CLAIM_KEY = "idpsource";
    private static final String TYP_CLAIM_KEY = "typ";
    private static final String AUTH_MACHINE_JWT = "authmachine+jwt";

    private final long tokenValidity;

    private final String issuer;

    private final Key signingKey;

    public CustomTokenProviderUtil(long tokenValidity, String privateKey, String issuer) throws NoSuchAlgorithmException {
        this.tokenValidity = tokenValidity;
        this.issuer = issuer;

        final KeyFactory rsa = KeyFactory.getInstance("RSA");

        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Decoders.BASE64.decode(privateKey));

        try {
            signingKey = rsa.generatePrivate(spec);
        } catch (InvalidKeySpecException e) {
            log.error("Error during generate private key", e);
            throw new IllegalStateException(e);
        }
    }

    public String createToken(String userExtId, String idpSource) {
        log.info("CreateToken with userExtId {} and idpSource {}", userExtId, idpSource);

        if (userExtId == null || idpSource == null) {
            throw new IllegalStateException("UserExtId and idpSource are required!");
        }

        final long nowMillis = System.currentTimeMillis();
        final Date now = new Date(nowMillis);

        final JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setNotBefore(now)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .claim(SCOPE_CLAIM_KEY, COVID_CERT_CREATION)
                .claim(USER_EXT_ID_CLAIM_KEY, userExtId)
                .claim(IDP_SOURCE_CLAIM_KEY, idpSource)
                .claim(TYP_CLAIM_KEY, AUTH_MACHINE_JWT)
                .signWith(signingKey);

        builder.setExpiration(new Date(nowMillis + tokenValidity));
        return builder.compact();
    }

    public String createTokenNotSigned(String userExtId, String idpSource) {
        log.info("CreateToken with userExtId {} and idpSource {}", userExtId, idpSource);

        if (userExtId == null || idpSource == null) {
            throw new IllegalStateException("UserExtId and idpSource are required!");
        }

        final long nowMillis = System.currentTimeMillis();
        final Date now = new Date(nowMillis);

        final JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setNotBefore(now)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .claim(SCOPE_CLAIM_KEY, COVID_CERT_CREATION)
                .claim(USER_EXT_ID_CLAIM_KEY, userExtId)
                .claim(IDP_SOURCE_CLAIM_KEY, idpSource);

        builder.setExpiration(new Date(nowMillis + tokenValidity));
        return builder.compact();
    }

}
