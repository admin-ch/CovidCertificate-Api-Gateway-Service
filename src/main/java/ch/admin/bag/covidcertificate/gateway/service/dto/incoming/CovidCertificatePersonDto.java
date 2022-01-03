package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CovidCertificatePersonDto {
    private CovidCertificatePersonNameDto name;
    @Schema(example= "1985-09-20", description = "birthdate of the covid certificate owner. Format: ISO 8601 date without time (e.g. 1985-09-20) OR YYYY-MM (e.g. 1985-09) OR YYYY (e.g. 1985).")
    private String dateOfBirth;
}
