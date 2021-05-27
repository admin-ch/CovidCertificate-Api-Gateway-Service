package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VaccinationCertificateDataDto {
    @Schema(example= "68267", description = "name of the medicinal product as registered in the country.")
    private String medicinalProductCode;
    @Schema(example= "2", description = "number in a series of doses.")
    private Integer numberOfDoses;
    @Schema(example= "2", description = "total series of doses.")
    private Integer totalNumberOfDoses;
    @Schema(example= "2021-05-14", description = "date of vaccination. Format: ISO 8601 date without time. Range: can be between 1900-01-01 and 2099-12-31. Regexp: \"[19|20][0-9][0-9]-(0[1-9]|1[0-2])-([0-2][1-9]|3[0|1])\".")
    private LocalDate vaccinationDate;
    @Schema(example= "CH", description = "the country in which the covid certificate owner has been vaccinated. Format: string (2 chars according to ISO 3166 Country Codes).")
    private String countryOfVaccination;
}
