package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public abstract class CertificateCreateDto {
    @JsonUnwrapped
    private CovidCertificatePersonDto personData;
    @Schema(example = "de", description = "language for the PDF (together with english). Accepted languages are: de, it, fr, rm")
    private String language;
    private String otp;
    @Schema(description = "Address of the recipient. A printout of the certificate will be sent to the certificate holder if this parameter is passed. " +
            "The first line of the address is automatically filled with the givenName and familyName from the name attribute."
    )
    private CovidCertificateAddressDto address;
    private IdentityDto identity;
}
