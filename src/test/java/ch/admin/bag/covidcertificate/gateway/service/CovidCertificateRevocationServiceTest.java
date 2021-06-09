package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.RevokeCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CovidCertificateRevocationServiceTest {

    static final JFixture fixture = new JFixture();
    static final ObjectMapper objectMapper = new ObjectMapper();
    static MockWebServer mockManagementService;

    private CovidCertificateRevocationService revocationService;

    @BeforeAll
    static void setUp() throws IOException {
        mockManagementService = new MockWebServer();
        mockManagementService.start();
    }

    @BeforeEach
    void initialize() {
        this.revocationService = new CovidCertificateRevocationService(WebClient.create());
        ReflectionTestUtils.setField(this.revocationService, "serviceUri",
                String.format("http://localhost:%s", mockManagementService.getPort()));
    }

    @Test
    void createsVaccineCertificateSuccessfully() throws Exception {
        var mockResponseDto = fixture.create(RevocationDto.class);
        mockManagementService.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(RevocationDto.class);
        assertDoesNotThrow(() -> revocationService.createRevocation(createDto));
    }

    @Test
    void throwsCreateException__ifResponseIs400() throws Exception {
        var mockResponseDto = fixture.create(RestError.class);
        mockManagementService.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(RevocationDto.class);
        assertThrows(RevokeCertificateException.class, () -> revocationService.createRevocation(createDto));
    }

    @Test
    void throwsCreateException__ifResponseIs500() throws Exception {
        var mockResponseDto = fixture.create(RestError.class);
        mockManagementService.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody(objectMapper.writeValueAsString(mockResponseDto))
                .addHeader("Content-Type", "application/json"));

        var createDto = fixture.create(RevocationDto.class);
        assertThrows(RevokeCertificateException.class, () -> revocationService.createRevocation(createDto));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockManagementService.shutdown();
    }
}