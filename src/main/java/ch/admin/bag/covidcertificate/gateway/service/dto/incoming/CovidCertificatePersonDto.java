package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CovidCertificatePersonDto {
    private CovidCertificatePersonNameDto name;
    @Schema(example= "1950-06-04", description = "birthdate of the covid certificate owner. Format: ISO 8601 date without time. Range: can be between 1900-01-01 and 2099-12-31. Regexp: \"[19|20][0-9][0-9]-(0[1-9]|1[0-2])-([0-2][1-9]|3[0|1])\".")
    private LocalDate dateOfBirth;
}
