package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.service.ValueSetsService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CountryCodeDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CountryCodesDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableRapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableVaccineDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.SystemSource;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccineDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flextrade.jfixture.JFixture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@Slf4j
public class ValueSetsControllerTest {

    private static final String BASE_URL = "/api/v1/valuesets";
    private static final JFixture fixture = new JFixture();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ValueSetsService valueSetsService;

    @InjectMocks
    private ValueSetsController controller;
    private MockMvc mockMvc;

    @BeforeAll
    static void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    void initialize() {
        this.mockMvc = standaloneSetup(controller, new ResponseStatusExceptionHandler()).build();
    }

    @Nested
    class RapidTestValueSetTests {
        private static final String URL = BASE_URL + "/rapid-tests";

        private List<RapidTestDto> rapidTestDtoResponse;

        @BeforeEach()
        void initialize() {
            RapidTestDto rapidTestDto = fixture.create(RapidTestDto.class);
            this.rapidTestDtoResponse = List.of(rapidTestDto);
            when(valueSetsService.getRapidTests()).thenReturn(rapidTestDtoResponse);
        }

        @Test
        void getRapidTests_Success() throws Exception {
            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON)
                                                       .contentType(MediaType.APPLICATION_JSON))
                                      .andExpect(status().isOk());

            verify(valueSetsService, times(1)).getRapidTests();
        }
    }

    @Nested
    class IssuableRapidTestValueSetTests {
        private static final String URL = BASE_URL + "/issuable-rapid-tests";

        private List<IssuableRapidTestDto> issuableRapidTestDtoResponse;

        @BeforeEach()
        void initialize() {
            IssuableRapidTestDto issuableRapidTestDto = fixture.create(IssuableRapidTestDto.class);
            this.issuableRapidTestDtoResponse = List.of(issuableRapidTestDto);
            when(valueSetsService.getIssuableRapidTests()).thenReturn(issuableRapidTestDtoResponse);
        }

        @Test
        void getIssuableRapidTests_Success() throws Exception {
            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON)
                                                       .contentType(MediaType.APPLICATION_JSON))
                                      .andExpect(status().isOk());

            verify(valueSetsService, times(1)).getIssuableRapidTests();
        }
    }

    @Nested
    class VaccineValueSetTests {
        private static final String URL = BASE_URL + "/vaccines";

        private List<VaccineDto> vaccineDtoResponse;

        @BeforeEach()
        void initialize() {
            VaccineDto vaccineDto = fixture.create(VaccineDto.class);
            this.vaccineDtoResponse = List.of(vaccineDto);
            when(valueSetsService.getVaccines()).thenReturn(vaccineDtoResponse);
        }

        @Test
        void getVaccines_Success() throws Exception {
            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON)
                                                       .contentType(MediaType.APPLICATION_JSON))
                                      .andExpect(status().isOk());

            verify(valueSetsService, times(1)).getVaccines();
        }
    }

    @Nested
    class IssuableVaccineValueSetTests {
        private static final String URL = BASE_URL + "/issuable-vaccines";

        private List<IssuableVaccineDto> issuableVaccineDtoResponse;

        @BeforeEach()
        void initialize() {
            IssuableVaccineDto issuableVaccineDto = fixture.create(IssuableVaccineDto.class);
            this.issuableVaccineDtoResponse = List.of(issuableVaccineDto);
            when(valueSetsService.getIssuableVaccines(SystemSource.ApiGateway)).thenReturn(issuableVaccineDtoResponse);
        }

        @Test
        void getIssuableVaccines_Success() throws Exception {
            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON)
                                                       .contentType(MediaType.APPLICATION_JSON))
                                      .andExpect(status().isOk());

            verify(valueSetsService, times(1)).getIssuableVaccines(SystemSource.ApiGateway);
        }
    }

    @Nested
    class CountryCodeByValueSetTests {
        private static final String URL = BASE_URL + "/countries";

        private CountryCodesDto countryCodesDto;

        @BeforeEach()
        void initialize() {
            this.countryCodesDto = fixture.create(CountryCodesDto.class);
            when(valueSetsService.getCountryCodes()).thenReturn(countryCodesDto);
        }

        @Test
        void getCountryCodes_Success() throws Exception {
            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(valueSetsService, times(1)).getCountryCodes();
        }
    }

    @Nested
    class CountryCodesByLanguageValueSetTests {
        private static final String URL = BASE_URL + "/countries";

        private List<CountryCodeDto> countryCodeDtoList;

        @BeforeEach()
        void initialize() {
            CountryCodeDto countryCodeDto = fixture.create(CountryCodeDto.class);
            this.countryCodeDtoList = List.of(countryCodeDto);
            when(valueSetsService.getCountryCodesByLanguage(anyString())).thenReturn(countryCodeDtoList);
        }

        @Test
        void getCountryCodes_Success() throws Exception {
            mockMvc.perform(get(URL + "/de").accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(valueSetsService, times(1)).getCountryCodesByLanguage(anyString());
        }
    }
}
