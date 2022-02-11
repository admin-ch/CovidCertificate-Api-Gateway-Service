package ch.admin.bag.covidcertificate.gateway.web.controller.utils;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * For testing purposes only.
 */
@Hidden
@RestController
@Profile({"local"})
@RequestMapping(value = "/signature")
@Slf4j
public class SignatureTestController {

    private static final String PRIVATE_CERT_PATH = "/home/dev/private-cert.pem";
//    private static final String PRIVATE_CERT_PATH = "/home/dev/development/postman/ZH-spital-A-t.bit.admin.ch.key";
//    private static final String PRIVATE_CERT_PATH = "/home/dev/development/postman/a0115-irfBIT-PROD.key";

    /**
     * For testing purposes only. Creates a signature for the sent body.
     *
     * @param payload Data to be signed.
     * @return Base64 Signature of the data and Base64 encoded public key.
     */
    @PostMapping(consumes = {"text/plain"})
    public Map<String, String> create(@RequestBody String payload) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, InvalidKeySpecException {
        PrivateKey privateKey = this.getCertificate();

        Signature signature = Signature.getInstance("SHA256withRSA");

        Base64.Encoder encoder = Base64.getEncoder();

        String normalizedJson = payload.replaceAll("[\\n\\t ]", "");
        signature.initSign(privateKey);
        signature.update(normalizedJson.getBytes());
        String signatureString = encoder.encodeToString(signature.sign());

        HashMap<String, String> map = new HashMap<>();
        map.put("signature", signatureString);

        return map;
    }

    private PrivateKey getCertificate() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Path filePath = Paths.get(PRIVATE_CERT_PATH);
        byte[] privateKeyBytes = Files.readAllBytes(filePath);
        String privateKeyContent = new String(privateKeyBytes);
        privateKeyContent = privateKeyContent.replace("\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        return kf.generatePrivate(keySpecPKCS8);
    }

}
