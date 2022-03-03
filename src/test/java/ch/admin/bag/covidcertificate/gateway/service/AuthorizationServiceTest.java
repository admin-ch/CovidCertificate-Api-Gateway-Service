package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.client.FunctionAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.AuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.DtoWithAuthorization;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import com.flextrade.jfixture.JFixture;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    static final JFixture fixure = new JFixture();

    BearerTokenValidationService bearerTokenValidationService;
    IdentityAuthorizationClient identityAuthorizationClient;
    FunctionAuthorizationClient functionAuthorizationClient;

    DtoWithAuthorization dtoWithAuthorization;
    AuthorizationService authorizationService;
    List<String> allowedCommonNames = Arrays.asList("test-cn");
    String ipAddress;

    @BeforeEach
    void initialize() {
        this.bearerTokenValidationService = mock(BearerTokenValidationService.class);
        this.identityAuthorizationClient = mock(IdentityAuthorizationClient.class);
        this.functionAuthorizationClient = mock(FunctionAuthorizationClient.class);
        this.dtoWithAuthorization = this.getDtoWithAuthorization(false, false);

        

        this.authorizationService = new AuthorizationService(bearerTokenValidationService, identityAuthorizationClient, functionAuthorizationClient);
        this.ipAddress = fixure.create(String.class);
    }

    @Test
    void verifiesCommonName__ifInAllowedList() {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNamesForIdentity", allowedCommonNames);
        this.setCnNameInContext("test-cn");

        var uuid = assertDoesNotThrow(() -> authorizationService.validateAndGetId(dtoWithAuthorization, ipAddress, Function.CREATE_VACCINE_CERTIFICATE));
        verify(identityAuthorizationClient, times(1)).authorize(dtoWithAuthorization.getIdentity().getUuid(), dtoWithAuthorization.getIdentity().getIdpSource());
        assertEquals(dtoWithAuthorization.getIdentity().getUuid(), uuid);
    }

    @Test
    void verifiesOtp__ifNotInAllowedList() throws InvalidBearerTokenException {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNamesForIdentity", allowedCommonNames);
        this.setCnNameInContext("not-in-allowed");

        assertDoesNotThrow(() -> authorizationService.validateAndGetId(dtoWithAuthorization, ipAddress, Function.CREATE_VACCINE_CERTIFICATE));
        verify(bearerTokenValidationService, times(1)).validate(this.dtoWithAuthorization.getOtp(), ipAddress);
    }

    @Test
    void checksOtp__ifIdentityDtoIsNullAndInAllowedList() throws InvalidBearerTokenException {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNamesForIdentity", allowedCommonNames);
        this.setCnNameInContext("test-cn");

        var otherDtoWithAuth = this.getDtoWithAuthorization(true, false);

        assertDoesNotThrow(() -> authorizationService.validateAndGetId(otherDtoWithAuth, ipAddress, Function.CREATE_VACCINE_CERTIFICATE));
        verify(identityAuthorizationClient, never()).authorize(any(), any());
        verify(bearerTokenValidationService, never()).validate(this.dtoWithAuthorization.getOtp(), ipAddress);
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