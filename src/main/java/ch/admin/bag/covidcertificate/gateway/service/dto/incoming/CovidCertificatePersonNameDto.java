package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CovidCertificatePersonNameDto {
    @Schema(example= "Rochat", description = "family name of the covid certificate owner. Format: maxLength: 50 CHAR")
    private String familyName;
    @Schema(example= "Céline", description = "first name of the covid certificate owner. Format: maxLength: 50 CHAR")
    private String givenName;
}
