package ch.admin.bag.covidcertificate.gateway.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.mock.web.MockFilterChain;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

import static org.mockito.Mockito.*;

class IntegrityFilterTest {
    private static final String keyHeaderName = "X-Client-Cert";
    private static final String hashHeaderName = "X-Signature";

    private static PrivateKey privateKey;
    private static String certificateString;
    private static MessageDigest digest;
    private static Signature signature;

    private static final String testJson = "{\n" +
            "  \"name\": [\n" +
            "    {\n" +
            "      \"familyName\": \"Hans\",\n" +
            "      \"givenName\": \"Muster\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"dateOfBirth\": \"2000-05-10\",\n" +
            "  \"vaccinationInfo\": [\n" +
            "    {\n" +
            "      \"vaccineProphylaxis\": \"string\",\n" +
            "      \"medicinalProduct\": \"string\",\n" +
            "      \"marketingAuthorizationHolder\": \"string\",\n" +
            "      \"numberOfDoses\": 2,\n" +
            "      \"totalNumberOfDoses\": 2,\n" +
            "      \"vaccinationDate\": \"2021-05-09\",\n" +
            "      \"countryOfVaccination\": \"CH\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"healthProfessional\": {\n" +
            "    \"sourceIdp\": \"string\",\n" +
            "    \"idpuuid\": \"string\"\n" +
            "  }\n" +
            "}";

    private IntegrityFilter integrityFilter;
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final MockFilterChain mockFilterChain = mock(MockFilterChain.class);
    private final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);

    @BeforeAll
    public static void setUpTests() throws NoSuchAlgorithmException, CertificateException, OperatorCreationException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(4096);
        KeyPair pair = keyPairGen.generateKeyPair();
        privateKey = pair.getPrivate();
        certificateString = CertificateUtil.convertX509ToString(CertificateUtil.getX509Certificate(pair));
        digest = MessageDigest.getInstance("SHA-256");
        signature = Signature.getInstance("SHA256withRSA");
    }

    @BeforeEach
    public void setUp() throws IOException {
        this.integrityFilter = new IntegrityFilter(mockObjectMapper);
        reset(request, response, mockFilterChain);

        // recreate input stream for request
        InputStream inputStream = new ByteArrayInputStream(testJson.getBytes());
        ServletInputStream servletInputStream = new DelegatingServletInputStream(inputStream);
        when(request.getInputStream()).thenReturn(servletInputStream);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(testJson)));
        when(request.getCharacterEncoding()).thenReturn(StandardCharsets.UTF_8.name());
    }

    @Test
    void testSignatureValid() throws Exception {
        byte[] signature = getSignature();

        when(request.getHeader(keyHeaderName)).thenReturn(certificateString);
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(testJson)));

        integrityFilter.doFilterInternal(request, response, mockFilterChain);
        verify(mockFilterChain, times(1)).doFilter(any(), any()); // use any as the request is wrapped
        verify(response, never()).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, never()).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testSignatureValidWindowsWithNewLine() throws Exception {
        String json = "\r" + testJson + "\r";
        byte[] signature = getSignature(json, privateKey);

        when(request.getHeader(keyHeaderName)).thenReturn(certificateString);
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        integrityFilter.doFilterInternal(request, response, mockFilterChain);
        verify(mockFilterChain, times(1)).doFilter(any(), any()); // use any as the request is wrapped
        verify(response, never()).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, never()).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testSignatureValidWithTab() throws Exception {
        String json = "\t" + testJson + "\t";
        byte[] signature = getSignature(json, privateKey);

        when(request.getHeader(keyHeaderName)).thenReturn(certificateString);
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        integrityFilter.doFilterInternal(request, response, mockFilterChain);
        verify(mockFilterChain, times(1)).doFilter(any(), any()); // use any as the request is wrapped
        verify(response, never()).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, never()).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testWithOtherBody() throws Exception {
        byte[] signature = getSignature();

        when(request.getHeader(keyHeaderName)).thenReturn(certificateString);
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));

        InputStream inputStream = new ByteArrayInputStream("other body".getBytes());
        ServletInputStream servletInputStream = new DelegatingServletInputStream(inputStream);
        when(request.getInputStream()).thenReturn(servletInputStream);

        integrityFilter.doFilterInternal(request, response, mockFilterChain);
        verify(mockFilterChain, never()).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testEncryptedWithWrongPrivateKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair otherKeyPair = keyGen.generateKeyPair();
        PrivateKey otherPrivateKey = otherKeyPair.getPrivate();

        byte[] signature = getSignature(testJson, otherPrivateKey);

        when(request.getHeader(keyHeaderName)).thenReturn(certificateString);
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));

        integrityFilter.doFilterInternal(request, response, mockFilterChain);
        verify(mockFilterChain, never()).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testWrongPublicKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair otherKeyPair = keyGen.generateKeyPair();

        byte[] signature = getSignature();

        when(request.getHeader(keyHeaderName)).thenReturn(CertificateUtil.convertX509ToString(CertificateUtil.getX509Certificate(otherKeyPair)));
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));

        integrityFilter.doFilterInternal(request, response, mockFilterChain);
        verify(mockFilterChain, never()).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testWithOtherHash() throws Exception {
        byte[] signature = getSignature("other json", privateKey);

        when(request.getHeader(keyHeaderName)).thenReturn(certificateString);
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));

        integrityFilter.doFilterInternal(request, response, mockFilterChain);
        verify(mockFilterChain, never()).doFilter(request, response);
        verify(response, times(1)).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testWithoutHeaders() throws Exception {
        byte[] signature = getSignature();

        when(request.getHeader(keyHeaderName)).thenReturn(null);
        when(request.getHeader(hashHeaderName)).thenReturn(Base64.getEncoder().encodeToString(signature));
        integrityFilter.doFilterInternal(request, response, mockFilterChain);

        when(request.getHeader(keyHeaderName)).thenReturn(certificateString);
        when(request.getHeader(hashHeaderName)).thenReturn(null);
        integrityFilter.doFilterInternal(request, response, mockFilterChain);

        when(request.getHeader(keyHeaderName)).thenReturn(null);
        when(request.getHeader(hashHeaderName)).thenReturn(null);
        integrityFilter.doFilterInternal(request, response, mockFilterChain);

        verify(mockFilterChain, never()).doFilter(request, response);
        verify(response, times(3)).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response, times(3)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    private byte[] getSignature() throws SignatureException, InvalidKeyException {
        return this.getSignature(testJson, privateKey);
    }

    private byte[] getSignature(String json, PrivateKey privateKey) throws SignatureException, InvalidKeyException {
        String normalizedJson = json.replaceAll("[\\n\\r\\t ]", "");
        signature.initSign(privateKey);
        signature.update(normalizedJson.getBytes());
        return signature.sign();
    }
}
