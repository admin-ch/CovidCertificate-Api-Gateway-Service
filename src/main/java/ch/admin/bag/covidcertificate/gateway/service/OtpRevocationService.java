package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.domain.OtpRevocation;
import ch.admin.bag.covidcertificate.gateway.domain.OtpRevocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpRevocationService {

    private static final String OTP_CACHE_NAME = "otps";

    private final OtpRevocationRepository otpRevocationRepository;
    private final CacheManager cacheManager;

    public boolean isRevoked(String jti) {
        return getOtpRevocations()
                .stream()
                .anyMatch(otpRevocation -> otpRevocation.getJti().equals(jti));
    }

    @Cacheable(OTP_CACHE_NAME)
    public List<OtpRevocation> getOtpRevocations() {
        return otpRevocationRepository.findAll();
    }

    @Scheduled(fixedRateString = "${cc-api-gateway-service.cache-duration}")
    public void clearCache() {
        Cache cache = cacheManager.getCache(OTP_CACHE_NAME);
        if (cache != null) {
            cache.clear();
        }
    }

}
