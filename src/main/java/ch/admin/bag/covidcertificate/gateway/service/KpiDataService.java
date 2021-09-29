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

    public void saveKpiData(LocalDateTime timestamp, String type, String value, String uvci, String details, String country) {
        KpiData kpiData = new KpiData(timestamp, type, value, uvci, details, country);
        logRepository.save(kpiData);
    }
}
