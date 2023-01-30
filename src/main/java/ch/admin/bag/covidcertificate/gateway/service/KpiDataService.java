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

    public void saveKpiData(LocalDateTime timestamp, String type, String value) {
        saveKpiData(timestamp, type, value, null, null, null, false, null);
    }

    public void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci, String details, String country) {
        saveKpiData(timestamp, type, value, uvci, details, country, false, null);
    }

    private void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci, String details, String country,
                             boolean fraud, String inAppDeliveryCode) {
        KpiData kpiData = KpiData.builder()
                .timestamp(timestamp)
                .type(type)
                .value(value)
                .uvci(uvci)
                .details(details)
                .country(country)
                .fraud(fraud)
                .inAppDeliveryCode(inAppDeliveryCode)
                .build();
        logRepository.save(kpiData);
    }
}
