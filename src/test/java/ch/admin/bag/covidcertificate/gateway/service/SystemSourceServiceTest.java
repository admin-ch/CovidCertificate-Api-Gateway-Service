package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.SystemSource;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemSourceServiceTest {

    static final JFixture fixture = new JFixture();
    static final ObjectMapper objectMapper = new ObjectMapper();

    private SystemSourceService systemSourceService;

    @BeforeAll
    static void setUp() throws IOException {
    }

    @BeforeEach
    void initialize() {
        this.systemSourceService = new SystemSourceService();
        ReflectionTestUtils.setField(this.systemSourceService, "allowedCommonNamesForSystemSource", List.of("cn-authorized"));
    }

    @Nested
    class GetRelevantSystemSource {
        @Test
        @DisplayName("GIVEN 'cn-authorized' WHEN input is ApiPlatform THEN relevantSystemSource is ApiPlatform")
        void testSystemSourceApiPlatform_authorized() throws Exception {
            setCommonName("cn-authorized");

            SystemSource systemSource = systemSourceService.getRelevantSystemSource(SystemSource.ApiPlatform);

            assertEquals(SystemSource.ApiPlatform, systemSource);
        }

        @Test
        @DisplayName("GIVEN 'cn-not-authorized' WHEN input is ApiPlatform THEN relevantSystemSource is ApiGateway")
        void testSystemSourceApiPlatform_unauthorized() throws Exception {
            setCommonName("cn-not-authorized");

            SystemSource systemSource = systemSourceService.getRelevantSystemSource(SystemSource.ApiPlatform);

            assertEquals(SystemSource.ApiGateway, systemSource);
        }

        @Test
        @DisplayName("GIVEN 'cn-not-authorized' WHEN input is any THEN relevantSystemSource is ApiGateway")
        void testSystemSource_unauthorized() throws Exception {
            setCommonName("cn-not-authorized");

            SystemSource systemSource = systemSourceService.getRelevantSystemSource(fixture.create(SystemSource.class));

            assertEquals(SystemSource.ApiGateway, systemSource);
        }
    }

    private void setCommonName(String commonName) {
        CustomHeaderAuthenticationToken authentication = mock(CustomHeaderAuthenticationToken.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getId()).thenReturn(commonName);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
