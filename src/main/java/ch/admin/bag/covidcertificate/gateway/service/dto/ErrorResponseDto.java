package ch.admin.bag.covidcertificate.gateway.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ErrorResponseDto {

    private String status;

    private String message;

    private String error;
}
