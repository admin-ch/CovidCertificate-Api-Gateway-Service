package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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
}
