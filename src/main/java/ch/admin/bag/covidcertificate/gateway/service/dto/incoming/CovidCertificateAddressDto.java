package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class CovidCertificateAddressDto {
    @Schema(required = true, description = "Recipient of the certificate", example = "Hans Muster")
    private String line1;
    @Schema(description = "Optional second address line")
    private String line2;
    @Schema(required = true, example = "3000", maxLength = 4, minLength = 4)
    private String zipCode;
    @Schema(required = true, example = "Bern")
    private String city;
}
