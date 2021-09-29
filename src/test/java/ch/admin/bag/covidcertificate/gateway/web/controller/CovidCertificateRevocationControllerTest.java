package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateRevocationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.KpiDataService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_REVOKE_CERTIFICATE_TYPE;
import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_TYPE_RECOVERY;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_BEARER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
class CovidCertificateRevocationControllerTest {

    private static final String URL = "/api/v1/covidcertificate/revoke";
    private static final JFixture fixture = new JFixture();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CovidCertificateRevocationService revocationService;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private KpiDataService kpiDataService;


    @InjectMocks
    private CovidCertificateRevocationController controller;

    private RevocationDto revocationDto;
    private MockMvc mockMvc;

    @BeforeAll
    static void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    void initialize() {
        this.revocationDto = fixture.create(RevocationDto.class);
        this.mockMvc = standaloneSetup(controller, new ResponseStatusExceptionHandler()).build();
    }

    @Test
    void revokesCertificateSuccessfully__withOtp() throws Exception {
        ReflectionTestUtils.setField(this.revocationDto, "identity", null);

        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(this.revocationDto)))
                .andExpect(status().isCreated());

        verify(authorizationService, times(1)).validateAndGetId(any(), any());
        verify(revocationService, times(1)).createRevocation(any(RevocationDto.class));
        verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_REVOKE_CERTIFICATE_TYPE), any(), anyString(),isNull(),isNull());
    }

    @Test
    void revokesCertificateSuccessfully__withIdentity() throws Exception {
        ReflectionTestUtils.setField(this.revocationDto, "otp", null);

        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(this.revocationDto)))
                .andExpect(status().isCreated());

        verify(authorizationService, times(1)).validateAndGetId(any(), any());
        verify(revocationService, times(1)).createRevocation(any(RevocationDto.class));
        verify(kpiDataService, times(1)).saveKpiData(any(), eq(KPI_REVOKE_CERTIFICATE_TYPE), any(), anyString(),isNull(),isNull());
    }

    @Test
    void returns403__withAuthorizationError() throws Exception {
        ReflectionTestUtils.setField(this.revocationDto, "otp", null);
        when(authorizationService.validateAndGetId(any(), any())).thenThrow(new InvalidBearerTokenException(INVALID_BEARER));

        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(this.revocationDto)))
                .andExpect(status().isForbidden());

        verify(authorizationService, times(1)).validateAndGetId(any(), any());
        verify(revocationService, never()).createRevocation(any(RevocationDto.class));
        verify(kpiDataService, never()).saveKpiData(any(), eq(KPI_TYPE_RECOVERY), any(), anyString(),isNull(),isNull());
    }
}
