package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.service.BearerTokenValidationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateGenerationService;
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
    private BearerTokenValidationService tokenValidationService;
    @Mock
    private KpiDataService kpiDataService;
    @Mock
    private IdentityAuthorizationClient identityAuthorizationClient;

    @InjectMocks
    private CovidCertificateGenerationController controller;
    private MockMvc mockMvc;

    @BeforeAll
    static void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        customizeVaccinationCertificateCreateDto(fixture);
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

        @BeforeEach()
        void initialize() {
            this.vaccineCreateDto = fixture.create(VaccinationCertificateCreateDto.class);
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtp() throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isOk());

            verify(tokenValidationService, times(1)).validate(eq(this.vaccineCreateDto.getOtp()));
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any());
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtpAndAddress() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isOk());

            verify(tokenValidationService, times(1)).validate(eq(this.vaccineCreateDto.getOtp()));
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any());
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any());
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

            verify(identityAuthorizationClient, times(1)).authorize(eq(identityDto.getUuid()), eq(identityDto.getIdpSource()));
            verify(tokenValidationService, never()).validate(any());
            verify(generationService, times(1)).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any());
        }

        @Test
        void returns403__withNoAuthorization() throws Exception {
            ReflectionTestUtils.setField(this.vaccineCreateDto, "otp", null);

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isForbidden());

            verify(tokenValidationService, never()).validate(any());
            verify(identityAuthorizationClient, never()).authorize(any(), any());
            verify(generationService, never()).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any());
        }

        @Test
        void returns403__withBothAuthorizations() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.vaccineCreateDto, "identity", identityDto);

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.vaccineCreateDto)))
                    .andExpect(status().isForbidden());

            verify(tokenValidationService, never()).validate(any());
            verify(identityAuthorizationClient, never()).authorize(any(), any());
            verify(generationService, never()).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any());
        }
    }

    @Nested
    class CreateTestCertificateTests {
        private static final String URL = BASE_URL + "test";

        private TestCertificateCreateDto testCreateDto;

        @BeforeEach()
        void initialize() {
            this.testCreateDto = fixture.create(TestCertificateCreateDto.class);
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtp() throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isOk());

            verify(tokenValidationService, times(1)).validate(eq(this.testCreateDto.getOtp()));
            verify(generationService, times(1)).createCovidCertificate(any(TestCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_TEST), any());
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtpAndAddress() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isOk());

            verify(tokenValidationService, times(1)).validate(eq(this.testCreateDto.getOtp()));
            verify(generationService, times(1)).createCovidCertificate(any(TestCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_TEST), any());
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any());
        }

        @Test
        void createsVaccineCertificateSuccessfully__withIdentity() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.testCreateDto, "identity", identityDto);
            ReflectionTestUtils.setField(this.testCreateDto, "otp", "");

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isOk());

            verify(identityAuthorizationClient, times(1)).authorize(eq(identityDto.getUuid()), eq(identityDto.getIdpSource()));
            verify(tokenValidationService, never()).validate(any());
            verify(generationService, times(1)).createCovidCertificate(any(TestCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_TEST), any());
        }

        @Test
        void returns403__withNoAuthorization() throws Exception {
            ReflectionTestUtils.setField(this.testCreateDto, "otp", null);

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isForbidden());

            verify(tokenValidationService, never()).validate(any());
            verify(identityAuthorizationClient, never()).authorize(any(), any());
            verify(generationService, never()).createCovidCertificate(any(TestCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_TEST), any());
        }

        @Test
        void returns403__withBothAuthorizations() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.testCreateDto, "identity", identityDto);

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.testCreateDto)))
                    .andExpect(status().isForbidden());

            verify(tokenValidationService, never()).validate(any());
            verify(identityAuthorizationClient, never()).authorize(any(), any());
            verify(generationService, never()).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any());
        }
    }

    @Nested
    class CreateRecoveryCertificateTests {
        private static final String URL = BASE_URL + "recovery";

        private RecoveryCertificateCreateDto recoveryCreateDto;

        @BeforeEach()
        void initialize() {
            this.recoveryCreateDto = fixture.create(RecoveryCertificateCreateDto.class);
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtp() throws Exception {
            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isOk());

            verify(tokenValidationService, times(1)).validate(eq(this.recoveryCreateDto.getOtp()));
            verify(generationService, times(1)).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any());
        }

        @Test
        void createsVaccineCertificateSuccessfully__withOtpAndAddress() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "address", fixture.create(CovidCertificateAddressDto.class));

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isOk());

            verify(tokenValidationService, times(1)).validate(eq(this.recoveryCreateDto.getOtp()));
            verify(generationService, times(1)).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any());
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_CANTON), any());
        }

        @Test
        void createsVaccineCertificateSuccessfully__withIdentity() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.recoveryCreateDto, "identity", identityDto);
            ReflectionTestUtils.setField(this.recoveryCreateDto, "otp", "");

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isOk());

            verify(identityAuthorizationClient, times(1)).authorize(eq(identityDto.getUuid()), eq(identityDto.getIdpSource()));
            verify(tokenValidationService, never()).validate(any());
            verify(generationService, times(1)).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any());
        }

        @Test
        void returns403__withNoAuthorization() throws Exception {
            ReflectionTestUtils.setField(this.recoveryCreateDto, "otp", null);

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isForbidden());

            verify(tokenValidationService, never()).validate(any());
            verify(identityAuthorizationClient, never()).authorize(any(), any());
            verify(generationService, never()).createCovidCertificate(any(RecoveryCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any());
        }

        @Test
        void returns403__withBothAuthorizations() throws Exception {
            var identityDto = fixture.create(IdentityDto.class);
            ReflectionTestUtils.setField(this.recoveryCreateDto, "identity", identityDto);

            mockMvc.perform(post(URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(this.recoveryCreateDto)))
                    .andExpect(status().isForbidden());

            verify(tokenValidationService, never()).validate(any());
            verify(identityAuthorizationClient, never()).authorize(any(), any());
            verify(generationService, never()).createCovidCertificate(any(VaccinationCertificateCreateDto.class));
            verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_VACCINATION), any());
        }
    }
}
