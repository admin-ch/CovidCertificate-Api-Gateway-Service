package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.domain.OtpRevocation;
import ch.admin.bag.covidcertificate.gateway.domain.OtpRevocationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class OtpRevocationServiceTest {

    private OtpRevocationService service;

    private static OtpRevocationRepository otpRevocationRepository;
    private static CacheManager cacheManager;
    private static final String revokedJti = "ThisIsATestJti";

    @BeforeAll
    static void setUp() {
        otpRevocationRepository = mock(OtpRevocationRepository.class);
        cacheManager = mock(CacheManager.class);
        var otpRevocation = mock(OtpRevocation.class);
        when(otpRevocation.getJti()).thenReturn(revokedJti);
        when(otpRevocationRepository.findAll()).thenReturn(List.of(otpRevocation));
    }

    @Test
    void isRevokedTest_revoked() {
        service = new OtpRevocationService(otpRevocationRepository, cacheManager);

        boolean isRevoked = service.isRevoked(revokedJti);

        assertTrue(isRevoked);
    }

    @Test
    void isRevokedTest_notRevoked() {
        service = new OtpRevocationService(otpRevocationRepository, cacheManager);

        boolean isRevoked = service.isRevoked("not revoked");

        assertFalse(isRevoked);
    }

    @Test
    void clearCache_withCache() {
        var cache = mock(Cache.class);
        when(cacheManager.getCache("otps")).thenReturn(cache);
        service = new OtpRevocationService(otpRevocationRepository, cacheManager);

        service.clearCache();

        verify(cache).clear();
    }

    @Test
    void clearCache_withoutCache() {
        when(cacheManager.getCache("otps")).thenReturn(null);
        service = new OtpRevocationService(otpRevocationRepository, cacheManager);

        service.clearCache();
    }
}
