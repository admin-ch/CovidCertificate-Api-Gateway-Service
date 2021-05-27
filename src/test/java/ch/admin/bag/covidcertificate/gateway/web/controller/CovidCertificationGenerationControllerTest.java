package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateGenerationService;
import ch.admin.bag.covidcertificate.gateway.service.dto.AuthorizationCodeCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationCertificateCreateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@Slf4j
@Disabled
class CovidCertificationGenerationControllerTest {

    private static final String URL = "/api/code/v1";

    @Mock
    private CovidCertificateGenerationService service;

    @InjectMocks
    private CovidCertificationGenerationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = standaloneSetup(controller).build();
    }

    @Test
    void createAuthCode() throws Exception {
        //given
        when(service.createCovidCertificate(any(VaccinationCertificateCreateDto.class))).thenReturn(null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());

        //when
        mockMvc.perform(post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new AuthorizationCodeCreateDto("todo", "todo"))))
                .andExpect(status().isOk())
                .andExpect(content().bytes("{\"authorizationCode\":\"1234\"}".getBytes()));
    }

}
