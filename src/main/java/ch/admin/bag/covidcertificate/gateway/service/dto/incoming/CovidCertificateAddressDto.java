package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class CovidCertificateAddressDto {
    @Schema(required = true, description = "Street and number of the certificate holder.", example = "Musterweg 4b")
    private String streetAndNr;
    @Schema(required = true, example = "3000", maxLength = 4, minLength = 4, type = "integer")
    private String zipCode;
    @Schema(required = true, example = "Bern")
    private String city;
    @Schema(required = true, description = "Abbreviation of the canton issuing the certificate. " +
            "This will be used as the sender of the paper based delivery.", example = "BE"
    )
    private String cantonCodeSender;
}
