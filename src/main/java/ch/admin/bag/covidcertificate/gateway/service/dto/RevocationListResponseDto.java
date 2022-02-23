package ch.admin.bag.covidcertificate.gateway.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class RevocationListResponseDto {
    private Map<String, String> uvcisToErrorMessage;
    private List<String> revokedUvcis;
}
