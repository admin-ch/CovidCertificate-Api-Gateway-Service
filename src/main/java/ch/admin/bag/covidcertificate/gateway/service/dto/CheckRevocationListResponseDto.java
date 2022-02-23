package ch.admin.bag.covidcertificate.gateway.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CheckRevocationListResponseDto {
    private Map<String, String> uvciToErrorMessage;
    private Map<String, String> uvciToWarningMessage;
    private List<String> revocableUvcis;
}
