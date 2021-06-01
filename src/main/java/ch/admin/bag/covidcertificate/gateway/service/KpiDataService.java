package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.domain.KpiData;
import ch.admin.bag.covidcertificate.gateway.domain.KpiDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class KpiDataService {

    private final KpiDataRepository logRepository;

    public void saveKpiData(LocalDateTime kpiTimestamp, String type, String value) {
        KpiData kpiData = new KpiData(
                kpiTimestamp,
                type,
                value
        );
        logRepository.save(kpiData);
    }
}
