package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CovidCertificateGenerationServiceTest {

    static final JFixture fixture = new JFixture();
    static final ObjectMapper objectMapper = new ObjectMapper();
    static MockWebServer mockManagementService;

    private static SystemSourceService systemSourceService;

    private CovidCertificateGenerationService generationService;

    @BeforeAll
    static void setUp() throws IOException {
        systemSourceService = mock(SystemSourceService.class);
        mockManagementService = new MockWebServer();
        mockManagementService.start();
    }

    @BeforeEach
    void initialize() {
        Mockito.reset(systemSourceService);
        this.generationService = new CovidCertificateGenerationService(WebClient.create(), systemSourceService);
        ReflectionTestUtils.setField(this.generationService, "serviceUri",
                String.format("http://localhost:%s/", mockManagementService.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockManagementService.shutdown();
    }

    @Nested
    class Create_VaccinationCertificateCreateDto {
        @Test
        void createsVaccineCertificateSuccessfully() throws Exception {
            SystemSource systemSource = fixture.create(SystemSource.class);
            when(systemSourceService.getRelevantSystemSource(any())).thenReturn(systemSource);
            var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            mockManagementService.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(mockResponseDto))
                    .addHeader("Content-Type", "application/json"));

            var createDto = fixture.create(VaccinationCertificateCreateDto.class);
            var response = generationService.createCovidCertificate(createDto, fixture.create(String.class));

            String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
            assertTrue(recordedRequest.contains("\"systemSource\":\"" + systemSource.name() + "\""));

            assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
            assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
            assertEquals(mockResponseDto.getUvci(), response.getUvci());
        }

        @Test
        void throwsCreateException__ifResponseIs400() throws Exception {
            SystemSource systemSource = fixture.create(SystemSource.class);
            when(systemSourceService.getRelevantSystemSource(any())).thenReturn(systemSource);
            var mockResponseDto = fixture.create(RestError.class);
            mockManagementService.enqueue(new MockResponse()
                    .setResponseCode(HttpStatus.BAD_REQUEST.value())
                    .setBody(objectMapper.writeValueAsString(mockResponseDto))
                    .addHeader("Content-Type", "application/json"));

            var createDto = fixture.create(VaccinationCertificateCreateDto.class);
            assertThrows(CreateCertificateException.class, () -> generationService.createCovidCertificate(createDto, fixture.create(String.class)));
        }

        @Test
        void throwsCreateException__ifResponseIs500() throws Exception {
            SystemSource systemSource = fixture.create(SystemSource.class);
            when(systemSourceService.getRelevantSystemSource(any())).thenReturn(systemSource);
            var mockResponseDto = fixture.create(RestError.class);
            mockManagementService.enqueue(new MockResponse()
                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .setBody(objectMapper.writeValueAsString(mockResponseDto))
                    .addHeader("Content-Type", "application/json"));

            var createDto = fixture.create(VaccinationCertificateCreateDto.class);
            assertThrows(CreateCertificateException.class, () -> generationService.createCovidCertificate(createDto, fixture.create(String.class)));

            String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
            assertTrue(recordedRequest.contains("\"systemSource\":\"" + systemSource.name() + "\""));
        }

        @Test
        void throwsIllegalStateException__ifResponseBodyIsEmpty() throws Exception {
            SystemSource systemSource = fixture.create(SystemSource.class);
            when(systemSourceService.getRelevantSystemSource(any())).thenReturn(systemSource);
            mockManagementService.enqueue(new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .addHeader("Content-Type", "application/json"));

            var createDto = fixture.create(VaccinationCertificateCreateDto.class);
            assertThrows(IllegalStateException.class, () -> generationService.createCovidCertificate(createDto, fixture.create(String.class)));

            String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
            assertTrue(recordedRequest.contains("\"systemSource\":\"" + systemSource.name() + "\""));
        }
    }

    @Nested
    class Create_VaccinationTouristCertificateCreateDto {
        @Test
        void createsVaccinationTouristCertificateSuccessfully() throws Exception {
            SystemSource systemSource = fixture.create(SystemSource.class);
            when(systemSourceService.getRelevantSystemSource(any())).thenReturn(systemSource);
            var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            mockManagementService.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(mockResponseDto))
                    .addHeader("Content-Type", "application/json"));

            var createDto = fixture.create(VaccinationTouristCertificateCreateDto.class);
            var response = generationService.createCovidCertificate(createDto, fixture.create(String.class));

            String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
            assertTrue(recordedRequest.contains("\"systemSource\":\"" + systemSource.name() + "\""));

            assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
            assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
            assertEquals(mockResponseDto.getUvci(), response.getUvci());
        }
    }


    @Nested
    class Create_TestCertificateCreateDto {
        @Test
        void createsTestCertificateSuccessfully() throws Exception {
            SystemSource systemSource = fixture.create(SystemSource.class);
            when(systemSourceService.getRelevantSystemSource(any())).thenReturn(systemSource);

            var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            mockManagementService.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(mockResponseDto))
                    .addHeader("Content-Type", "application/json"));

            var createDto = fixture.create(TestCertificateCreateDto.class);
            var response = generationService.createCovidCertificate(createDto, fixture.create(String.class));

            String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
            assertTrue(recordedRequest.contains("\"systemSource\":\"" + systemSource.name() + "\""));

            assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
            assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
            assertEquals(mockResponseDto.getUvci(), response.getUvci());
        }
    }

    @Nested
    class Create_RecoveryCertificateCreateDto {
        @Test
        void createsRecoveryCertificateSuccessfully() throws Exception {
            SystemSource systemSource = fixture.create(SystemSource.class);
            when(systemSourceService.getRelevantSystemSource(any())).thenReturn(systemSource);

            var mockResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            mockManagementService.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(mockResponseDto))
                    .addHeader("Content-Type", "application/json"));

            var createDto = fixture.create(RecoveryCertificateCreateDto.class);
            var response = generationService.createCovidCertificate(createDto, fixture.create(String.class));

            String recordedRequest = mockManagementService.takeRequest().getBody().readString(Charset.defaultCharset());
            assertTrue(recordedRequest.contains("\"systemSource\":\"" + systemSource.name() + "\""));

            assertArrayEquals(mockResponseDto.getPdf(), response.getPdf());
            assertArrayEquals(mockResponseDto.getQrCode(), response.getQrCode());
            assertEquals(mockResponseDto.getUvci(), response.getUvci());
        }
    }
}
