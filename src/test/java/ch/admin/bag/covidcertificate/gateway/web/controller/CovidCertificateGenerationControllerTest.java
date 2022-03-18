package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.domain.TestType;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateGenerationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.KpiDataService;
import ch.admin.bag.covidcertificate.gateway.service.dto.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.AntibodyCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CovidCertificateAddressDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CovidCertificateCreateResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryRatCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.TestCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.TestCertificateDataDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationTouristCertificateCreateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flextrade.jfixture.JFixture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;
import java.util.Collections;

import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_CANTON;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_ANTIBODY;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_IN_APP_DELIVERY;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_RECOVERY;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_RECOVERY_RAT;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_TEST;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_VACCINATION;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_VACCINATION_TOURIST;
import static ch.admin.bag.covidcertificate.gateway.FixtureCustomization.customizeAntibodyCertificateCreateDto;
import static ch.admin.bag.covidcertificate.gateway.FixtureCustomization.customizeRecoveryCertificateCreateDto;
import static ch.admin.bag.covidcertificate.gateway.FixtureCustomization.customizeRecoveryRatCertificateCreateDto;
import static ch.admin.bag.covidcertificate.gateway.FixtureCustomization.customizeTestCertificateCreateDto;
import static ch.admin.bag.covidcertificate.gateway.FixtureCustomization.customizeVaccinationCertificateCreateDto;
import static ch.admin.bag.covidcertificate.gateway.FixtureCustomization.customizeVaccinationTouristCertificateCreateDto;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_BEARER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@Slf4j
class CovidCertificateGenerationControllerTest {

    private static final String BASE_URL = "/api/v1/covidcertificate/";
    private static final JFixture fixture = new JFixture();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CovidCertificateGenerationService generationService;
    @Mock
    private KpiDataService kpiDataService;
    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private CovidCertificateGenerationController controller;
    private MockMvc mockMvc;

    @BeforeAll
    static void setUp() {

        mapper.registerModule(new JavaTimeModule());
        customizeVaccinationCertificateCreateDto(fixture);
        customizeVaccinationTouristCertificateCreateDto(fixture);
        customizeTestCertificateCreateDto(fixture);
        customizeRecoveryCertificateCreateDto(fixture);
        customizeRecoveryRatCertificateCreateDto(fixture);
        customizeAntibodyCertificateCreateDto(fixture);
    }

    @BeforeEach
    void initialize() {
        this.mockMvc = standaloneSetup(controller, new ResponseStatusExceptionHandler()).build();
    }

//    @Test
//    @Disabled
//    void createAuthCode() throws Exception {
//        //given
//        when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class), any(String.class))).thenReturn(null);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//        mapper.registerModule(new JavaTimeModule());
//
//        //when
//        mockMvc.perform(post("/api/code/v1")
//                .accept(MediaType.APPLICATION_JSON)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(new AuthorizationCodeCreateDto("todo", "todo"))))
//                .andExpect(status().isOk())
//                .andExpect(content().bytes("{\"authorizationCode\":\"1234\"}".getBytes()));
//    }

    @Nested
    class CreateVaccineCertificateTests {
        private static final String URL = BASE_URL + "vaccination";

        private VaccinationCertificateCreateDto vaccineCreateDto;

        @BeforeEach()
        void initialize() {
            this.vaccineCreateDto = fixture.create(VaccinationCertificateCreateDto.class);
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", null);
            ReflectionTestUtils.setField(this.vaccineCreateDto, "appCode", null);
            CovidCertificateCreateResponseDto createResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class), any(String.class))).thenReturn(createResponseDto);
            when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class), eq(null))).thenReturn(createResponseDto);
        }

        @Test
        void createsTestCertificateSuccessfully() throws Exception {
            postRequest(URL, this.vaccineCreateDto, status().isOk());
        }

        @Test
        void callsAuthorizationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.vaccineCreateDto);
            postRequest(URL, this.vaccineCreateDto, status().isOk());
            verify(authorizationService, times(1)).validateAndGetId(equalsSerialized(payload), any(), any());
        }

        @Test
        void callsAuthorizationServiceWithRemoteAddressFromRequest() throws Exception {
            var payload = mapper.writeValueAsString(this.vaccineCreateDto);

            var remoteAddress = fixture.create(String.class);
            mockMvc.perform(post(URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(remoteAddress);
                                return request;
                            }))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), eq(remoteAddress), any());
        }

        @Test
        void callsGenerationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.vaccineCreateDto);

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(generationService, times(1)).createCovidCertificate((VaccinationCertificateCreateDto) equalsSerialized(payload), eq(null));
        }

        @Test
        void savesPrintKpiWithCurrentTimestamp_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.vaccineCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(eq(now), eq(KPI_CANTON), any(), anyString(), any(), anyString());
            }
        }

        @Test
        void savesPrintKpiWithCantonSender_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var cantonSender = this.vaccineCreateDto.getAddress().getCantonCodeSender();

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), eq(cantonSender), anyString(), any(), anyString());
        }

        @Test
        void savesPrintKpiWithGeneratedUvci_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), eq(certificate.getUvci()), any(), anyString());
        }

        @Test
        void savesPrintKpiWithProductCode_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), eq(this.vaccineCreateDto.getVaccinationInfo().get(0).getMedicinalProductCode()), anyString());
        }

        @Test
        void savesPrintKpiWithCorrectCountry_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), eq(vaccineCreateDto.getVaccinationInfo().get(0).getCountryOfVaccination()));
        }


        @Test
        void shouldNotSavesPrintKpi_whenAddressIsNotSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", null);

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), anyString());
        }

        @Test
        void savesInAppKpiWithCurrentTimestamp_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "appCode", fixture.create(String.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.vaccineCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(
                        eq(now), eq(KPI_TYPE_IN_APP_DELIVERY), any(), anyString(), any(), anyString(), anyString());
            }
        }

        @Test
        void savesInAppKpiWithUserId_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "appCode", fixture.create(String.class));
            var userExtId = fixture.create(String.class);
            when(authorizationService.validateAndGetId(any(), any(), any())).thenReturn(userExtId);

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), eq(userExtId), anyString(), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithGeneratedUvci_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "appCode", fixture.create(String.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), eq(certificate.getUvci()), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithProductCode_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "appCode", fixture.create(String.class));
            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(),
                    eq(this.vaccineCreateDto.getVaccinationInfo().get(0).getMedicinalProductCode()),
                    anyString(), anyString());
        }


        @Test
        void savesInAppKpiWithCorrectCountry_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(),
                    eq(vaccineCreateDto.getVaccinationInfo().get(0).getCountryOfVaccination()), anyString());
        }

        @Test
        void shouldNotSavesInAppKpi_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "appCode", null);
            postRequest(URL, this.vaccineCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(), anyString());
        }

        @Test
        void returns403_withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            postRequest(URL, this.vaccineCreateDto, status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any(), any());
            verify(generationService, never()).createCovidCertificate(any(VaccinationCertificateCreateDto.class), eq(null));
        }

        @Test
        void returnsGeneratedCertificate() throws Exception {
            var certificateResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class), eq(null))).thenReturn(certificateResponseDto);

            var actual = postRequest(URL, this.vaccineCreateDto, status().isOk());

            assertEquals(mapper.writeValueAsString(certificateResponseDto), actual.getResponse().getContentAsString());
        }
    }

    @Nested
    class CreateVaccineTouristCertificateTests {
        private static final String URL = BASE_URL + "vaccination-tourist";

        private VaccinationTouristCertificateCreateDto vaccineTouristCreateDto;

        @BeforeEach()
        void initialize() {
            this.vaccineTouristCreateDto = fixture.create(VaccinationTouristCertificateCreateDto.class);
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", null);
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "appCode", null);
            CovidCertificateCreateResponseDto createResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class), any(String.class))).thenReturn(createResponseDto);
            when(generationService.createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class), eq(null))).thenReturn(createResponseDto);
        }

        @Test
        void createsTestCertificateSuccessfully() throws Exception {
            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());
        }

        @Test
        void callsAuthorizationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.vaccineTouristCreateDto);
            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());
            verify(authorizationService, times(1)).validateAndGetId(equalsSerialized(payload), any(), any());
        }

        @Test
        void callsAuthorizationServiceWithRemoteAddressFromRequest() throws Exception {
            var payload = mapper.writeValueAsString(this.vaccineTouristCreateDto);

            var remoteAddress = fixture.create(String.class);
            mockMvc.perform(post(URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(remoteAddress);
                                return request;
                            }))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), eq(remoteAddress), any());
        }

        @Test
        void callsGenerationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.vaccineTouristCreateDto);

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(generationService, times(1)).createCovidCertificate((VaccinationTouristCertificateCreateDto) equalsSerialized(payload), eq(null));
        }

        @Test
        void savesPrintKpiWithCurrentTimestamp_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(eq(now), eq(KPI_CANTON), any(), anyString(), any(), anyString());
            }
        }

        @Test
        void savesPrintKpiWithCantonSender_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var cantonSender = this.vaccineTouristCreateDto.getAddress().getCantonCodeSender();

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), eq(cantonSender), anyString(), any(), anyString());
        }

        @Test
        void savesPrintKpiWithGeneratedUvci_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), eq(certificate.getUvci()), any(), anyString());
        }

        @Test
        void savesPrintKpiWithProductCode_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), eq(this.vaccineTouristCreateDto.getVaccinationTouristInfo().get(0).getMedicinalProductCode()), anyString());
        }

        @Test
        void savesPrintKpiWithCorrectCountry_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), eq(vaccineTouristCreateDto.getVaccinationTouristInfo().get(0).getCountryOfVaccination()));
        }


        @Test
        void shouldNotSavesPrintKpi_whenAddressIsNotSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", null);

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), anyString());
        }

        @Test
        void savesInAppKpiWithCurrentTimestamp_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "appCode", fixture.create(String.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(
                        eq(now), eq(KPI_TYPE_IN_APP_DELIVERY), any(), anyString(), any(), anyString(), anyString());
            }
        }

        @Test
        void savesInAppKpiWithUserId_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "appCode", fixture.create(String.class));
            var userExtId = fixture.create(String.class);
            when(authorizationService.validateAndGetId(any(), any(), any())).thenReturn(userExtId);

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), eq(userExtId), anyString(), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithGeneratedUvci_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "appCode", fixture.create(String.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), eq(certificate.getUvci()), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithProductCode_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "appCode", fixture.create(String.class));
            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(),
                    eq(this.vaccineTouristCreateDto.getVaccinationTouristInfo().get(0).getMedicinalProductCode()),
                    anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithCorrectCountry_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(),
                    eq(vaccineTouristCreateDto.getVaccinationTouristInfo().get(0).getCountryOfVaccination()), anyString());
        }

        @Test
        void shouldNotSavesInAppKpi_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "appCode", null);
            postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(), anyString());
        }

        @Test
        void returns403_withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            postRequest(URL, this.vaccineTouristCreateDto, status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any(), any());
            verify(generationService, never()).createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class), eq(null));
        }

        @Test
        void returnsGeneratedCertificate() throws Exception {
            var certificateResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class), eq(null))).thenReturn(certificateResponseDto);

            var actual = postRequest(URL, this.vaccineTouristCreateDto, status().isOk());

            assertEquals(mapper.writeValueAsString(certificateResponseDto), actual.getResponse().getContentAsString());
        }
    }

    @Nested
    class CreateTestCertificateTests {
        private static final String URL = BASE_URL + "test";

        private TestCertificateCreateDto testCreateDto;

        @BeforeEach()
        void initialize() throws JsonProcessingException {
            this.testCreateDto = fixture.create(TestCertificateCreateDto.class);
            ReflectionTestUtils.setField(this.testCreateDto, "address", null);
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", null);
            CovidCertificateCreateResponseDto testCreateResponse = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(TestCertificateCreateDto.class), any(String.class))).thenReturn(testCreateResponse);
            when(generationService.createCovidCertificate(any(TestCertificateCreateDto.class), eq(null))).thenReturn(testCreateResponse);
        }

        @Test
        void createsTestCertificateSuccessfully() throws Exception {
            postRequest(URL, this.testCreateDto, status().isOk());
        }

        @Test
        void callsAuthorizationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.testCreateDto);
            postRequest(URL, this.testCreateDto, status().isOk());
            verify(authorizationService, times(1)).validateAndGetId(equalsSerialized(payload), any(), any());
        }

        @Test
        void callsAuthorizationServiceWithRemoteAddressFromRequest() throws Exception {
            var payload = mapper.writeValueAsString(this.testCreateDto);

            var remoteAddress = fixture.create(String.class);
            mockMvc.perform(post(URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(remoteAddress);
                                return request;
                            }))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), eq(remoteAddress), any());
        }

        @Test
        void callsGenerationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.testCreateDto);

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(generationService, times(1)).createCovidCertificate((TestCertificateCreateDto) equalsSerialized(payload), eq(null));
        }

        @Test
        void savesPrintKpiWithCurrentTimestamp_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.testCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(eq(now), eq(KPI_CANTON), any(), anyString(), any(), anyString());
            }
        }

        @Test
        void savesPrintKpiWithCantonSender_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var cantonSender = this.testCreateDto.getAddress().getCantonCodeSender();

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), eq(cantonSender), anyString(), any(), anyString());
        }

        @Test
        void savesPrintKpiWithGeneratedUvci_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(TestCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), eq(certificate.getUvci()), any(), anyString());
        }

        @EnumSource(value = TestType.class)
        @ParameterizedTest
        void savesPrintKpiWithCorrectDetails_whenAddressIsSetAndTypeCodeMatches(TestType type) throws Exception {
            var certificateData = fixture.create(TestCertificateDataDto.class);
            ReflectionTestUtils.setField(certificateData, "typeCode", type.typeCode);
            ReflectionTestUtils.setField(this.testCreateDto, "testInfo", Collections.singletonList(certificateData));
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), eq(type.kpiValue), anyString());
        }

        @EnumSource(value = TestType.class)
        @ParameterizedTest
        void savesPrintKpiWithCorrectDetails_whenAddressIsSetAndTypeCodeMatchesCaseInsensitive(TestType type) throws Exception {
            var certificateData = fixture.create(TestCertificateDataDto.class);
            ReflectionTestUtils.setField(certificateData, "typeCode", type.typeCode.toLowerCase());
            ReflectionTestUtils.setField(this.testCreateDto, "testInfo", Collections.singletonList(certificateData));
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), eq(type.kpiValue), anyString());
        }

        @Test
        void savesPrintKpiWithCorrectCountry_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), eq(testCreateDto.getTestInfo().get(0).getMemberStateOfTest()));
        }


        @Test
        void shouldNotSavesPrintKpi_whenAddressIsNotSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "address", null);

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), anyString());
        }

        @Test
        void savesInAppKpiWithCurrentTimestamp_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", fixture.create(String.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.testCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(
                        eq(now), eq(KPI_TYPE_IN_APP_DELIVERY), any(), anyString(), any(), anyString(), anyString());
            }
        }

        @Test
        void savesInAppKpiWithUserId_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", fixture.create(String.class));
            var userExtId = fixture.create(String.class);
            when(authorizationService.validateAndGetId(any(), any(), any())).thenReturn(userExtId);

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), eq(userExtId), anyString(), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithGeneratedUvci_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", fixture.create(String.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(TestCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), eq(certificate.getUvci()), any(), anyString(), anyString());
        }

        @EnumSource(value = TestType.class)
        @ParameterizedTest
        void savesInAppKpiWithCorrectDetails_whenAppCodeIsSetAndTypeCodeMatches(TestType type) throws Exception {
            var certificateData = fixture.create(TestCertificateDataDto.class);
            ReflectionTestUtils.setField(certificateData, "typeCode", type.typeCode);
            ReflectionTestUtils.setField(this.testCreateDto, "testInfo", Collections.singletonList(certificateData));
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), eq(type.kpiValue), anyString(), anyString());
        }

        @EnumSource(value = TestType.class)
        @ParameterizedTest
        void savesInAppKpiWithCorrectDetails_whenAppCodeIsSetAndTypeCodeMatchesCaseInsensitive(TestType type) throws Exception {
            var certificateData = fixture.create(TestCertificateDataDto.class);
            ReflectionTestUtils.setField(certificateData, "typeCode", type.typeCode.toLowerCase());
            ReflectionTestUtils.setField(this.testCreateDto, "testInfo", Collections.singletonList(certificateData));
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), eq(type.kpiValue), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithCorrectCountry_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(),
                    eq(testCreateDto.getTestInfo().get(0).getMemberStateOfTest()), anyString());
        }


        @Test
        void shouldNotSavesInAppKpi_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "appCode", null);
            postRequest(URL, this.testCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(), anyString());
        }

        @Test
        void returns403_withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            postRequest(URL, this.testCreateDto, status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any(), any());
            verify(generationService, never()).createCovidCertificate(any(RecoveryCertificateCreateDto.class), eq(null));
        }

        @Test
        void returnsGeneratedCertificate() throws Exception {
            var certificateResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(TestCertificateCreateDto.class), eq(null))).thenReturn(certificateResponseDto);

            var actual = postRequest(URL, this.testCreateDto, status().isOk());

            assertEquals(mapper.writeValueAsString(certificateResponseDto), actual.getResponse().getContentAsString());
        }
    }

    @Nested
    class CreateRecoveryCertificateTests {
        private static final String URL = BASE_URL + "recovery";

        private RecoveryCertificateCreateDto recoveryCreateDto;

        @BeforeEach()
        void initialize() {
            this.recoveryCreateDto = fixture.create(RecoveryCertificateCreateDto.class);
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", null);
            ReflectionTestUtils.setField(this.recoveryCreateDto, "appCode", null);
            CovidCertificateCreateResponseDto createResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryCertificateCreateDto.class), any(String.class))).thenReturn(createResponseDto);
            when(generationService.createCovidCertificate(any(RecoveryCertificateCreateDto.class), eq(null))).thenReturn(createResponseDto);
        }

        @Test
        void createsTestCertificateSuccessfully() throws Exception {
            postRequest(URL, this.recoveryCreateDto, status().isOk());
        }

        @Test
        void callsAuthorizationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.recoveryCreateDto);
            postRequest(URL, this.recoveryCreateDto, status().isOk());
            verify(authorizationService, times(1)).validateAndGetId(equalsSerialized(payload), any(), any());
        }

        @Test
        void callsAuthorizationServiceWithRemoteAddressFromRequest() throws Exception {
            var payload = mapper.writeValueAsString(this.recoveryCreateDto);

            var remoteAddress = fixture.create(String.class);
            mockMvc.perform(post(URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(remoteAddress);
                                return request;
                            }))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), eq(remoteAddress), any());
        }

        @Test
        void callsGenerationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.recoveryCreateDto);

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(generationService, times(1)).createCovidCertificate((RecoveryCertificateCreateDto) equalsSerialized(payload), eq(null));
        }

        @Test
        void savesPrintKpiWithCurrentTimestamp_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.recoveryCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(eq(now), eq(KPI_CANTON), any(), anyString(), any(), anyString());
            }
        }

        @Test
        void savesPrintKpiWithCantonSender_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var cantonSender = this.recoveryCreateDto.getAddress().getCantonCodeSender();

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), eq(cantonSender), anyString(), any(), anyString());
        }

        @Test
        void savesPrintKpiWithGeneratedUvci_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), eq(certificate.getUvci()), any(), anyString());
        }

        @Test
        void savesPrintKpiWithCorrectCountry_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), eq(recoveryCreateDto.getRecoveryInfo().get(0).getCountryOfTest()));
        }


        @Test
        void shouldNotSavesPrintKpi_whenAddressIsNotSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", null);

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), anyString());
        }

        @Test
        void savesInAppKpiWithCurrentTimestamp_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "appCode", fixture.create(String.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.recoveryCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(
                        eq(now), eq(KPI_TYPE_IN_APP_DELIVERY), any(), anyString(), any(), anyString(), anyString());
            }
        }

        @Test
        void savesInAppKpiWithUserId_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "appCode", fixture.create(String.class));
            var userExtId = fixture.create(String.class);
            when(authorizationService.validateAndGetId(any(), any(), any())).thenReturn(userExtId);

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), eq(userExtId), anyString(), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithGeneratedUvci_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "appCode", fixture.create(String.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), eq(certificate.getUvci()), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithCorrectCountry_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(),
                    eq(recoveryCreateDto.getRecoveryInfo().get(0).getCountryOfTest()), anyString());
        }

        @Test
        void shouldNotSavesInAppKpi_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "appCode", null);
            postRequest(URL, this.recoveryCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(), anyString());
        }

        @Test
        void returns403_withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            postRequest(URL, this.recoveryCreateDto, status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any(), any());
            verify(generationService, never()).createCovidCertificate(any(RecoveryCertificateCreateDto.class), eq(null));
        }

        @Test
        void returnsGeneratedCertificate() throws Exception {
            var certificateResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryCertificateCreateDto.class), eq(null))).thenReturn(certificateResponseDto);

            var actual = postRequest(URL, this.recoveryCreateDto, status().isOk());

            assertEquals(mapper.writeValueAsString(certificateResponseDto), actual.getResponse().getContentAsString());
        }
    }

    @Nested
    class CreateRecoveryRatCertificateTests {
        private static final String URL = BASE_URL + "recovery-rat";

        private RecoveryRatCertificateCreateDto recoveryRatCertificateCreateDto;

        @BeforeEach()
        void initialize() {
            this.recoveryRatCertificateCreateDto = fixture.create(RecoveryRatCertificateCreateDto.class);
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "address", null);
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "appCode", null);
            CovidCertificateCreateResponseDto createResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryRatCertificateCreateDto.class), any(String.class))).thenReturn(createResponseDto);
            when(generationService.createCovidCertificate(any(RecoveryRatCertificateCreateDto.class), eq(null))).thenReturn(createResponseDto);
        }

        @Test
        void createsTestCertificateSuccessfully() throws Exception {
            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());
        }

        @Test
        void callsAuthorizationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.recoveryRatCertificateCreateDto);
            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());
            verify(authorizationService, times(1)).validateAndGetId(equalsSerialized(payload), any(), any());
        }

        @Test
        void callsAuthorizationServiceWithRemoteAddressFromRequest() throws Exception {
            var payload = mapper.writeValueAsString(this.recoveryRatCertificateCreateDto);

            var remoteAddress = fixture.create(String.class);
            mockMvc.perform(post(URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(remoteAddress);
                                return request;
                            }))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), eq(remoteAddress), any());
        }

        @Test
        void callsGenerationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.recoveryRatCertificateCreateDto);

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(generationService, times(1)).createCovidCertificate((RecoveryRatCertificateCreateDto) equalsSerialized(payload), eq(null));
        }

        @Test
        void savesPrintKpiWithCurrentTimestamp_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(eq(now), eq(KPI_CANTON), any(), anyString(), any(), anyString());
            }
        }

        @Test
        void savesPrintKpiWithCantonSender_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var cantonSender = this.recoveryRatCertificateCreateDto.getAddress().getCantonCodeSender();

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), eq(cantonSender), anyString(), any(), anyString());
        }

        @Test
        void savesPrintKpiWithGeneratedUvci_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryRatCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), eq(certificate.getUvci()), any(), anyString());
        }

        @Test
        void savesPrintKpiWithCorrectCountry_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), eq("CH"));
        }


        @Test
        void shouldNotSavesPrintKpi_whenAddressIsNotSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "address", null);

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), anyString());
        }

        @Test
        void savesInAppKpiWithCurrentTimestamp_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "appCode", fixture.create(String.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(
                        eq(now), eq(KPI_TYPE_IN_APP_DELIVERY), any(), anyString(), any(), anyString(), anyString());
            }
        }

        @Test
        void savesInAppKpiWithUserId_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "appCode", fixture.create(String.class));
            var userExtId = fixture.create(String.class);
            when(authorizationService.validateAndGetId(any(), any(), any())).thenReturn(userExtId);

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY),
                                                         eq(userExtId), anyString(), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithGeneratedUvci_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "appCode", fixture.create(String.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryRatCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), eq(certificate.getUvci()), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithCorrectCountry_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(
                    any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(), eq("CH"), anyString());
        }

        @Test
        void shouldNotSavesInAppKpi_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.recoveryRatCertificateCreateDto, "appCode", null);
            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(), anyString());
        }

        @Test
        void returns403_withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            postRequest(URL, this.recoveryRatCertificateCreateDto, status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any(), any());
            verify(generationService, never()).createCovidCertificate(any(RecoveryRatCertificateCreateDto.class), eq(null));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(), anyString(), anyString());
        }

        @Test
        void returnsGeneratedCertificate() throws Exception {
            var certificateResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryRatCertificateCreateDto.class), eq(null))).thenReturn(certificateResponseDto);

            var actual = postRequest(URL, this.recoveryRatCertificateCreateDto, status().isOk());

            assertEquals(mapper.writeValueAsString(certificateResponseDto), actual.getResponse().getContentAsString());
        }
    }

    @Nested
    class CreateAntibodyCertificateTests {
        private static final String URL = BASE_URL + "antibody";

        private AntibodyCertificateCreateDto antibodyCreateDto;

        @BeforeEach()
        void initialize() {
            this.antibodyCreateDto = fixture.create(AntibodyCertificateCreateDto.class);
            ReflectionTestUtils.setField(this.antibodyCreateDto, "address", null);
            ReflectionTestUtils.setField(this.antibodyCreateDto, "appCode", null);
            CovidCertificateCreateResponseDto createResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(AntibodyCertificateCreateDto.class), any(String.class))).thenReturn(createResponseDto);
            when(generationService.createCovidCertificate(any(AntibodyCertificateCreateDto.class), eq(null))).thenReturn(createResponseDto);
        }

        @Test
        void createsTestCertificateSuccessfully() throws Exception {
            postRequest(URL, this.antibodyCreateDto, status().isOk());
        }

        @Test
        void callsAuthorizationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.antibodyCreateDto);
            postRequest(URL, this.antibodyCreateDto, status().isOk());
            verify(authorizationService, times(1)).validateAndGetId(equalsSerialized(payload), any(), any());
        }

        @Test
        void callsAuthorizationServiceWithRemoteAddressFromRequest() throws Exception {
            var payload = mapper.writeValueAsString(this.antibodyCreateDto);

            var remoteAddress = fixture.create(String.class);
            mockMvc.perform(post(URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(remoteAddress);
                                return request;
                            }))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), eq(remoteAddress), any());
        }

        @Test
        void callsGenerationServiceWithGivenPayload() throws Exception {
            var payload = mapper.writeValueAsString(this.antibodyCreateDto);

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(generationService, times(1)).createCovidCertificate((AntibodyCertificateCreateDto) equalsSerialized(payload), eq(null));
        }

        @Test
        void savesPrintKpiWithCurrentTimestamp_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.antibodyCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(eq(now), eq(KPI_CANTON), any(), anyString(), any(), anyString());
            }
        }

        @Test
        void savesPrintKpiWithCantonSender_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var cantonSender = this.antibodyCreateDto.getAddress().getCantonCodeSender();

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), eq(cantonSender), anyString(), any(), anyString());
        }

        @Test
        void savesPrintKpiWithGeneratedUvci_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(AntibodyCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), eq(certificate.getUvci()), any(), anyString());
        }

        @Test
        void savesPrintKpiWithCorrectCountry_whenAddressIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), eq("CH"));
        }


        @Test
        void shouldNotSavesPrintKpi_whenAddressIsNotSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "address", null);

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_CANTON), any(), any(), any(), anyString());
        }

        @Test
        void savesInAppKpiWithCurrentTimestamp_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "appCode", fixture.create(String.class));
            var now = LocalDateTime.now();
            try (MockedStatic<LocalDateTime> localDateTimeMock = Mockito.mockStatic(LocalDateTime.class)) {
                localDateTimeMock.when(LocalDateTime::now).thenReturn(now);

                postRequest(URL, this.antibodyCreateDto, status().isOk());

                verify(kpiDataService, times(1)).saveKpiData(eq(now), eq(KPI_TYPE_IN_APP_DELIVERY),
                                                             any(), anyString(), any(), anyString(), anyString());
            }
        }

        @Test
        void savesInAppKpiWithUserId_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "appCode", fixture.create(String.class));
            var userExtId = fixture.create(String.class);
            when(authorizationService.validateAndGetId(any(), any(), any())).thenReturn(userExtId);

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY),
                                                         eq(userExtId), anyString(), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithGeneratedUvci_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "appCode", fixture.create(String.class));
            var certificate = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(AntibodyCertificateCreateDto.class), eq(null))).thenReturn(certificate);

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY),
                                                         any(), eq(certificate.getUvci()), any(), anyString(), anyString());
        }

        @Test
        void savesInAppKpiWithCorrectCountry_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "appCode", fixture.create(String.class));

            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY),
                                                         any(), any(), any(), eq("CH"), anyString());
        }

        @Test
        void shouldNotSavesInAppKpi_whenAppCodeIsSet() throws Exception {
            ReflectionTestUtils.setField(this.antibodyCreateDto, "appCode", null);
            postRequest(URL, this.antibodyCreateDto, status().isOk());

            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_IN_APP_DELIVERY), any(), any(), any(), anyString());
        }

        @Test
        void returns403_withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            postRequest(URL, this.antibodyCreateDto, status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any(), any());
            verify(generationService, never()).createCovidCertificate(any(AntibodyCertificateCreateDto.class), eq(null));
        }

        @Test
        void returnsGeneratedCertificate() throws Exception {
            var certificateResponseDto = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(AntibodyCertificateCreateDto.class), eq(null))).thenReturn(certificateResponseDto);

            var actual = postRequest(URL, this.antibodyCreateDto, status().isOk());

            assertEquals(mapper.writeValueAsString(certificateResponseDto), actual.getResponse().getContentAsString());
        }
    }

    private <T extends CertificateCreateDto> T equalsSerialized(String expected) {
        return argThat((T certificateCreateDto) -> {
                    try {
                        return mapper.writeValueAsString(certificateCreateDto).equalsIgnoreCase(expected);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        fail();
                        throw new RuntimeException();
                    }
                }
        );
    }

    private MvcResult postRequest(String url, CertificateCreateDto createDto, ResultMatcher matcher) throws Exception {
        var payload = mapper.writeValueAsString(createDto);

        return mockMvc.perform(post(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(matcher)
                .andReturn();
    }
}
