package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CovidCertificateCreateResponseDto {
    @Schema(type = "string", format = "byte")
    private byte[] pdf;
    @Schema(type = "string", format = "byte")
    private byte[] qrCode;
    private String uvci;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(required = false)
    private RestError appDeliveryError;
}
