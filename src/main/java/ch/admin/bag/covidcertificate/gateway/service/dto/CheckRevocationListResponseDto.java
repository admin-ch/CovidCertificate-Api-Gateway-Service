package ch.admin.bag.covidcertificate.gateway.service.dto;

import java.util.List;
import java.util.Map;

public class CheckRevocationListResponseDto {
    private Map<String, String> uvciToErrorMessage;
    private Map<String, String> uvciToWarningMessage;
    private List<String> revocableUvcis;
}
