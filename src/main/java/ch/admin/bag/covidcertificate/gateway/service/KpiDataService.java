package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.domain.KpiData;
import ch.admin.bag.covidcertificate.gateway.domain.KpiDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class KpiDataService {

    private final KpiDataRepository logRepository;

    public void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci,
            String details, String country) {
        KpiData kpiData = new KpiData(timestamp, type, value, uvci, details, country);
    public void saveKpiData(LocalDateTime timestamp, String type, String value) {
        saveKpiData(timestamp, type, value, null, null, null, false);
    }
    public void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci, boolean fraud) {
        saveKpiData(timestamp, type, value, uvci, null, null, fraud);
    }

    public void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci, String details, String country) {
        saveKpiData(timestamp, type, value, uvci, details, country, false);
    }

    private void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci, String details, String country, boolean fraud) {
        KpiData kpiData = new KpiData(timestamp, type, value, uvci, details, country, fraud);
        logRepository.save(kpiData);
    }

    public void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci,
            String details, String country, String inAppDeliveryCode) {
        KpiData kpiData = new KpiData(timestamp, type, value, uvci, details, country, inAppDeliveryCode);
        logRepository.save(kpiData);
    }
}
