package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.DtoWithAuthorization;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {

    static final JFixture fixure = new JFixture();

    BearerTokenValidationService bearerTokenValidationService;
    IdentityAuthorizationClient identityAuthorizationClient;

    DtoWithAuthorization dtoWithAuthorization;
    AuthorizationService authorizationService;
    List<String> allowedCommonNames = Arrays.asList("test-cn");

    @BeforeEach
    void initialize() {
        this.bearerTokenValidationService = mock(BearerTokenValidationService.class);
        this.identityAuthorizationClient = mock(IdentityAuthorizationClient.class);
        this.dtoWithAuthorization = this.getDtoWithAuthorization(false, false);
        this.authorizationService = new AuthorizationService(bearerTokenValidationService, identityAuthorizationClient);
    }

    @Test
    void verifiesCommonName__ifInAllowedList() {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNames", allowedCommonNames);
        this.setCnNameInContext("test-cn");

        var uuid = assertDoesNotThrow(() -> authorizationService.validateAndGetId(dtoWithAuthorization));
        verify(identityAuthorizationClient, times(1)).authorize(dtoWithAuthorization.getIdentity().getUuid(), dtoWithAuthorization.getIdentity().getIdpSource());
        assertEquals(dtoWithAuthorization.getIdentity().getUuid(), uuid);
    }

    @Test
    void verifiesOtp__ifInNotAllowedList() throws InvalidBearerTokenException {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNames", allowedCommonNames);
        this.setCnNameInContext("not-in-allowed");

        assertDoesNotThrow(() -> authorizationService.validateAndGetId(dtoWithAuthorization));
        verify(bearerTokenValidationService, times(1)).validate(this.dtoWithAuthorization.getOtp());
    }

    @Test
    void throwsException__ifIdentityDtoIsNullAndInAllowedList() {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNames", allowedCommonNames);
        this.setCnNameInContext("test-cn");

        var otherDtoWithAuth = this.getDtoWithAuthorization(true, false);

        var exception = assertThrows(InvalidBearerTokenException.class, () -> authorizationService.validateAndGetId(otherDtoWithAuth));
        verify(identityAuthorizationClient, never()).authorize(any(), any());
        assertEquals(INVALID_IDENTITY_USER_CODE, exception.getError().getErrorCode());
        assertEquals(INVALID_IDENTITY_USER_MESSAGE, exception.getError().getErrorMessage());
    }

    private void setCnNameInContext(String cnValue) {
        var authentication = mock(CustomHeaderAuthenticationToken.class);
        when(authentication.getId()).thenReturn(cnValue);
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private DtoWithAuthorization getDtoWithAuthorization(boolean identityDtoNull, boolean otpNull) {
        return new DtoWithAuthorization() {
            private final IdentityDto identityDto = identityDtoNull ? null : fixure.create(IdentityDto.class);
            private final String otp = otpNull ? null : fixure.create(String.class);

            @Override
            public IdentityDto getIdentity() {
                return this.identityDto;
            }

            @Override
            public String getOtp() {
                return this.otp;
            }
        };
    }

}