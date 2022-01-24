package ch.admin.bag.covidcertificate.gateway.service.dto;

import java.util.List;
import java.util.Map;

public class RevocationListResponseDto {
    private Map<String, String> uvcisToErrorMessage;
    private List<String> revokedUvcis;
}
