package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.domain.OtpRevocation;
import ch.admin.bag.covidcertificate.gateway.domain.OtpRevocationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OtpRevocationServiceTest {

    @Test
    void getOtpRevocationsTest() {
        OtpRevocationRepository otpRevocationRepository = mock(OtpRevocationRepository.class);
        var otpRevocation = mock(OtpRevocation.class);
        when(otpRevocationRepository.findAll()).thenReturn(List.of(otpRevocation));
        OtpRevocationService service = new OtpRevocationService(otpRevocationRepository);

        List<OtpRevocation> result = service.getOtpRevocations();
        assertEquals(otpRevocation, result.get(0));
    }
}
