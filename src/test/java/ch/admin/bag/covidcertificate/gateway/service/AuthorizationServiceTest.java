package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.client.IdentityAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.client.internal.FunctionAuthorizationClient;
import ch.admin.bag.covidcertificate.gateway.features.authorization.model.Function;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.DtoWithAuthorization;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import ch.admin.bag.covidcertificate.gateway.service.model.UserAuthorizationData;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    List<String> allowedCommonNames = List.of("test-cn");
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

        this.dtoWithAuthorization = this.getDtoWithAuthorization(false, true);
        UserAuthorizationData userAuthorizationData = new UserAuthorizationData(dtoWithAuthorization.getIdentity().getUuid(), dtoWithAuthorization.getIdentity().getIdpSource(), Collections.emptyList());

        when(identityAuthorizationClient.fetchUserAndGetAuthData(any(String.class), any(String.class)))
                .thenReturn(userAuthorizationData);


        var uuid = assertDoesNotThrow(() -> authorizationService.validateAndGetId(dtoWithAuthorization, ipAddress, Function.CREATE_VACCINE_CERTIFICATE));
        verify(identityAuthorizationClient, times(1)).fetchUserAndGetAuthData(dtoWithAuthorization.getIdentity().getUuid(), dtoWithAuthorization.getIdentity().getIdpSource());
        assertEquals(dtoWithAuthorization.getIdentity().getUuid(), uuid);
    }

    @Test
    void verifiesOtp__ifNotInAllowedList() throws InvalidBearerTokenException {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNamesForIdentity", allowedCommonNames);
        this.setCnNameInContext("not-in-allowed");

        when(bearerTokenValidationService.validateOtpAndGetAuthData(any(String.class), any(String.class)))
                .thenReturn(new UserAuthorizationData(any(String.class), any(String.class), Collections.emptyList()));

        assertDoesNotThrow(() -> authorizationService.validateAndGetId(dtoWithAuthorization, ipAddress, Function.CREATE_VACCINE_CERTIFICATE));
        verify(bearerTokenValidationService, times(1)).validateOtpAndGetAuthData(this.dtoWithAuthorization.getOtp(), ipAddress);
    }

    @Test
    void checksOtp__ifIdentityDtoIsNullAndInAllowedList() throws InvalidBearerTokenException {
        ReflectionTestUtils.setField(authorizationService, "allowedCommonNamesForIdentity", allowedCommonNames);
        this.setCnNameInContext("test-cn");

        var otherDtoWithAuth = this.getDtoWithAuthorization(true, false);

        when(bearerTokenValidationService.validateOtpAndGetAuthData(any(String.class), any(String.class)))
                .thenReturn(new UserAuthorizationData(any(String.class), any(String.class), Collections.emptyList()));

        assertDoesNotThrow(() -> authorizationService.validateAndGetId(otherDtoWithAuth, ipAddress, Function.CREATE_VACCINE_CERTIFICATE));
        verify(identityAuthorizationClient, never()).fetchUserAndGetAuthData(any(), any());
        verify(bearerTokenValidationService, never()).validateOtpAndGetAuthData(this.dtoWithAuthorization.getOtp(), ipAddress);
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