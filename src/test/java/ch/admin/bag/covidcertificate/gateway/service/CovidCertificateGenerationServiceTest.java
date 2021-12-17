package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CovidCertificateCreateResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.SystemSource;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.TestCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationTouristCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CovidCertificateGenerationServiceTest {

    static final JFixture fixture = new JFixture();
    static final ObjectMapper objectMapper = new ObjectMapper();
    static MockWebServer mockManagementService;

    private CovidCertificateGenerationService generationService;

    @BeforeAll
    static void setUp() throws IOException {
        mockManagementService = new MockWebServer();
        mockManagementService.start();
    }

    @BeforeEach
    void initialize() {
        this.generationService = new CovidCertificateGenerationService(WebClient.create());
        ReflectionTestUtils.setField(this.generationService, "serviceUri",
                String.format("http://localhost:%s/", mockManagementService.getPort()));
        ReflectionTestUtils.setField(this.generationService, "allowedCommonNamesForSystemSource", List.of("cn-authorized"));
    }

    @Test
    void createsVaccineCertificateSuccessfully() throws Exception {
        setCommonName("cn-not-authorized");
        var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
        mockManagementService.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(VaccinationCertificateCreateDto.class);
        var response = generationService.createCovidCertificate(createDto);

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiGateway\""));

        assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
        assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
        assertEquals(mockResponseDto.getUvci(), response.getUvci());
    }

    @Test
    void createsVaccineCertificateSuccessfullyWithApiPlatform() throws Exception {
        setCommonName("cn-authorized");
        var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
        mockManagementService.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(VaccinationCertificateCreateDto.class);
        createDto.setSystemSource(SystemSource.ApiPlatform);
        var response = generationService.createCovidCertificate(createDto);

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiPlatform\""));

        assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
        assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
        assertEquals(mockResponseDto.getUvci(), response.getUvci());
    }

    @Test
    void createsVaccineCertificateSuccessfullyWithWrongApiPlatform() throws Exception {
        setCommonName("cn-not-authorized");
        var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
        mockManagementService.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(VaccinationCertificateCreateDto.class);
        createDto.setSystemSource(SystemSource.ApiPlatform);
        var response = generationService.createCovidCertificate(createDto);

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiGateway\""));

        assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
        assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
        assertEquals(mockResponseDto.getUvci(), response.getUvci());
    }

    @Test
    void createsVaccinatioTouristCertificateSuccessfully() throws Exception {
        setCommonName("cn-not-authorized");
        var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
        mockManagementService.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(VaccinationTouristCertificateCreateDto.class);
        var response = generationService.createCovidCertificate(createDto);

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiGateway\""));

        assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
        assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
        assertEquals(mockResponseDto.getUvci(), response.getUvci());
    }

    @Test
    void createsTestCertificateSuccessfully() throws Exception {
        setCommonName("cn-not-authorized");
        var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
        mockManagementService.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(TestCertificateCreateDto.class);
        var response = generationService.createCovidCertificate(createDto);

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiGateway\""));

        assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
        assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
        assertEquals(mockResponseDto.getUvci(), response.getUvci());
    }

    @Test
    void createsRecoveryCertificateSuccessfully() throws Exception {
        setCommonName("cn-not-authorized");
        var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
        mockManagementService.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(RecoveryCertificateCreateDto.class);
        var response = generationService.createCovidCertificate(createDto);

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiGateway\""));

        assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
        assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
        assertEquals(mockResponseDto.getUvci(), response.getUvci());
    }

    @Test
    void throwsCreateException__ifResponseIs400() throws Exception {
        setCommonName("cn-not-authorized");
        var mockResponseDto = fixture.create(RestError.class);
        mockManagementService.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(VaccinationCertificateCreateDto.class);
        assertThrows(CreateCertificateException.class, () -> generationService.createCovidCertificate(createDto));
    }

    @Test
    void throwsCreateException__ifResponseIs500() throws Exception {
        setCommonName("cn-not-authorized");
        var mockResponseDto = fixture.create(RestError.class);
        mockManagementService.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(VaccinationCertificateCreateDto.class);
        assertThrows(CreateCertificateException.class, () -> generationService.createCovidCertificate(createDto));

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiGateway\""));
    }

    @Test
    void throwsIllegalStateException__ifResponseBodyIsEmpty() throws Exception {
        setCommonName("cn-not-authorized");
        mockManagementService.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(VaccinationCertificateCreateDto.class);
        assertThrows(IllegalStateException.class, () -> generationService.createCovidCertificate(createDto));

        String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
        assertTrue(recordedRequest.contains("\"systemSource\":\"ApiGateway\""));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockManagementService.shutdown();
    }

    private void setCommonName(String commonName) {
        CustomHeaderAuthenticationToken authentication = mock(CustomHeaderAuthenticationToken.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getId()).thenReturn(commonName);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

}
