package ch.admin.bag.covidcertificate.gateway.filters;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.stream.Collectors;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_SIGNATURE;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.SIGNATURE_PARSE;

@Slf4j
@RequiredArgsConstructor
public class IntegrityFilter extends OncePerRequestFilter {
    public static final String HEADER_KEY_NAME = "X-Client-Cert";
    public static final String HEADER_HASH_NAME = "X-Signature";
    private final ObjectMapper mapper;

    private static PublicKey getKey(byte[] key) throws KeyException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(key);
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
            return certificate.getPublicKey();
        } catch (Exception e) {
            log.warn("Unable to create public key from header", e);
            throw new KeyException(e);
        }
    }

    private static boolean checkIntegrity(CachedBodyHttpServletRequest request) throws SignatureParseException {
        Base64.Decoder decoder = Base64.getDecoder();
        String signaturePublicKey = request.getHeader(HEADER_KEY_NAME);
        String signatureHash = request.getHeader(HEADER_HASH_NAME);

        if (signaturePublicKey != null && signatureHash != null) {
            try {
                byte[] decodedPublicKey = decoder.decode(signaturePublicKey.getBytes(StandardCharsets.UTF_8));
                byte[] decodedHash = decoder.decode(signatureHash.getBytes(StandardCharsets.UTF_8));

                String body = request.getReader().lines().collect(Collectors.joining()).replaceAll("[\\n\\r\\t ]", "");
                PublicKey publicKey = getKey(decodedPublicKey);

                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initVerify(publicKey);
                signature.update(body.getBytes());

                return signature.verify(decodedHash);
            } catch (Exception e) {
                log.warn("Error while verifying request integrity", e);
            }
        } else {
            log.info("One or both integrity header/s are missing, not processing request");
        }
        throw new SignatureParseException();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);
        RestError restError = null;
        try {
            if (checkIntegrity(wrappedRequest)) {
                log.info("Integrity check successful, forwarding request");
                filterChain.doFilter(wrappedRequest, httpServletResponse);
            } else {
                log.info("Integrity check failed, not processing request: " + httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI());
                restError = INVALID_SIGNATURE;
            }
        } catch (SignatureParseException e) {
            log.info("Unable to parse signature, not processing request");
            restError = SIGNATURE_PARSE;
        } finally {
            if (restError != null) {
                httpServletResponse.setStatus(restError.getHttpStatus().value());
                httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                mapper.writeValue(httpServletResponse.getWriter(), restError);
            }
        }
    }

    private static class SignatureParseException extends Exception {
    }
}
