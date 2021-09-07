package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CovidCertificatePersonDto {
    private CovidCertificatePersonNameDto name;
    @Schema(example= "1964-03-14", description = "birthdate of the covid certificate owner. Format: ISO 8601 date without time.")
    private String dateOfBirth;
}
