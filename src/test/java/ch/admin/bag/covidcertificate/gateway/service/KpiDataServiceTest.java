package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.domain.KpiDataRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static ch.admin.bag.covidcertificate.gateway.Constants.KPI_COMMON_NAME_TYPE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class KpiDataServiceTest {

    @InjectMocks
    private KpiDataService kpiDataService;

    @Mock
    private KpiDataRepository kpiDataRepository;

    @Test
    void success_saveKpiData_less_Parameter() {
        kpiDataService.saveKpiData(LocalDateTime.now(), KPI_COMMON_NAME_TYPE, "0815-some_body-PROD");

        verify(kpiDataRepository, atLeast(1)).save(any());
    }

    @Test
    void fail_saveKpiData_less_Parameter() {
        when(kpiDataRepository.save(any())).thenThrow(new RuntimeException("mocked runtime exception"));

        assertThrows(RuntimeException.class, () -> {
            kpiDataService.saveKpiData(LocalDateTime.now(), KPI_COMMON_NAME_TYPE, "0815-some_body-PROD");
        });
    }

    @Test
    void success_saveKpiData_more_Parameter() {
        // type ad was written until 17.10.2022, that means method still there but unused
        kpiDataService.saveKpiData(LocalDateTime.now(), "ad", "4376224", "urn:uvci:01:CH:4757BD5A07E7A255B7139C8C", "rapid", "CH");

        verify(kpiDataRepository, atLeast(1)).save(any());
    }

    @Test
    void fail_saveKpiData_more_Parameter() {
        when(kpiDataRepository.save(any())).thenThrow(new RuntimeException("mocked runtime exception"));
        // type ad was written until 17.10.2022, that means method still there but unused

        assertThrows(RuntimeException.class, () -> {
            kpiDataService.saveKpiData(LocalDateTime.now(), "ad", "4376224", "urn:uvci:01:CH:4757BD5A07E7A255B7139C8C", "rapid", "CH");
        });
    }
}