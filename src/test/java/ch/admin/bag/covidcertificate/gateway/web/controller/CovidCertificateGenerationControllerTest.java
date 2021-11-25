package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateGenerationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.KpiDataService;
import ch.admin.bag.covidcertificate.gateway.service.dto.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flextrade.jfixture.JFixture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static ch.admin.bag.covidcertificate.gateway.Constants.*;
import static ch.admin.bag.covidcertificate.gateway.FixtureCustomization.*;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_BEARER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        customizeVaccinationCertificateCreateDto(fixture);
        customizeVaccinationTouristCertificateCreateDto(fixture);
        customizeTestCertificateCreateDto(fixture);
        customizeRecoveryCertificateCreateDto(fixture);
    }

    @BeforeEach
    void initialize() {
        this.mockMvc = standaloneSetup(controller, new ResponseStatusExceptionHandler()).build();
    }

    @Test
    @Disabled
    void createAuthCode() throws Exception {
        //given
        when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class))).thenReturn(null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());

        //when
        mockMvc.perform(post("/api/code/v1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new AuthorizationCodeCreateDto("todo", "todo"))))
                .andExpect(status().isOk())
                .andExpect(content().bytes("{\"authorizationCode\":\"1234\"}".getBytes()));
    }

    @Nested
    class CreateVaccineCertificateTests {
        private static final String URL = BASE_URL + "vaccination";

        private VaccinationCertificateCreateDto vaccineCreateDto;
        private CovidCertificateCreateResponseDto vaccineCreateResponse;

        @BeforeEach()
        void initialize() {
            this.vaccineCreateDto = fixture.create(VaccinationCertificateCreateDto.class);
            this.vaccineCreateResponse = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationCertificateCreateDto.class))).thenReturn(vaccineCreateResponse);
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtp() throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any(), anyString(), anyString(), anyString());
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtpAndAddress() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any(), anyString(), anyString(), anyString());
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), anyString(), anyString(), anyString());
        }

        @Test
        void createsVaccineCertificateSuccessfully__withIdentity() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.vaccineCreateDto, "identity", identityDto);
            ReflectionTestUtils.setField(this.vaccineCreateDto, "otp", "");

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any(), anyString(), anyString(), anyString());
        }

        @Test
        void returns403__withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, never()).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    class CreateVaccineTouristCertificateTests {
        private static final String URL = BASE_URL + "vaccination-tourist";

        private VaccinationTouristCertificateCreateDto vaccineTouristCreateDto;
        private CovidCertificateCreateResponseDto vaccineTouristCreateResponse;

        @BeforeEach()
        void initialize() {
            this.vaccineTouristCreateDto = fixture.create(VaccinationTouristCertificateCreateDto.class);
            this.vaccineTouristCreateResponse = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class))).thenReturn(vaccineTouristCreateResponse);
        }

        @Test
        void createsVaccineTouristCertificateSuccessfully__withOtp() throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineTouristCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION_TOURIST), any(), anyString(), anyString(), anyString());
        }

        @Test
        void createsVaccineTouristCertificateSuccessfully__withOtpAndAddress() throws Exception {
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineTouristCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION_TOURIST), any(), anyString(), anyString(), anyString());
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), anyString(), anyString(), anyString());
        }

        @Test
        void createsVaccineTouristCertificateSuccessfully__withIdentity() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "identity", identityDto);
            ReflectionTestUtils.setField(this.vaccineTouristCreateDto, "otp", "");

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineTouristCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationTouristCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION_TOURIST), any(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    class CreateTestCertificateTests {
        private static final String URL = BASE_URL + "test";

        private TestCertificateCreateDto testCreateDto;
        private CovidCertificateCreateResponseDto testCreateResponse;

        @BeforeEach()
        void initialize() {
            this.testCreateDto = fixture.create(TestCertificateCreateDto.class);
            this.testCreateResponse = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(TestCertificateCreateDto.class))).thenReturn(testCreateResponse);
        }

        @Test
        void createsTestCertificateSuccessfully__withOtp() throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(TestCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_TEST), any(), anyString(), isNull(), anyString());
        }

        @Test
        void createsTestCertificateSuccessfully__withOtpAndAddress() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(TestCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_TEST), any(), anyString(), isNull(), anyString());
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), anyString(), isNull(), anyString());
        }

        @Test
        void createsTestCertificateSuccessfully__withIdentity() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.testCreateDto, "identity", identityDto);
            ReflectionTestUtils.setField(this.testCreateDto, "otp", "");

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(TestCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_TEST), any(), anyString(), isNull(), anyString());
        }

        @Test
        void returns403__withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, never()).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    class CreateRecoveryCertificateTests {
        private static final String URL = BASE_URL + "recovery";

        private RecoveryCertificateCreateDto recoveryCreateDto;
        private CovidCertificateCreateResponseDto recoveryCreateResponse;

        @BeforeEach()
        void initialize() {
            this.recoveryCreateDto = fixture.create(RecoveryCertificateCreateDto.class);
            this.recoveryCreateResponse = fixture.create(CovidCertificateCreateResponseDto.class);
            when(generationService.createCovidCertificate(any(RecoveryCertificateCreateDto.class))).thenReturn(recoveryCreateResponse);
        }

        @Test
        void createsRecoveryCertificateSuccessfully__withOtp() throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(), isNull(), anyString());
        }

        @Test
        void createsRecoveryCertificateSuccessfully__withOtpAndAddress() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(), isNull(), anyString());
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any(), anyString(), isNull(), anyString());
        }

        @Test
        void createsRecoveryCertificateSuccessfully__withIdentity() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.recoveryCreateDto, "identity", identityDto);
            ReflectionTestUtils.setField(this.recoveryCreateDto, "otp", "");

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isOk());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, times(1)).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(), isNull(), anyString());
        }

        @Test
        void returns403__withAuthorizationError() throws Exception {
            when(authorizationService.validateAndGetId(any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isForbidden());

            verify(authorizationService, times(1)).validateAndGetId(any(), any());
            verify(generationService, never()).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(), anyString(), anyString());
        }
    }
}
