package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VaccinationTouristCertificateDataDto {
    @Schema(example= "EU/1/20/1507", description = "name of the medicinal product as registered in the country.")
    private String medicinalProductCode;
    @Schema(example= "2", description = "number in a series of doses.")
    private Integer numberOfDoses;
    @Schema(example= "2", description = "total series of doses.")
    private Integer totalNumberOfDoses;
    @Schema(example= "2021-05-14", description = "date of vaccination. Format: ISO 8601 date without time.")
    private LocalDate vaccinationDate;
    @Schema(example= "CH", description = "the country in which the covid certificate owner has been vaccinated. Format: string (2 chars according to ISO 3166 Country Codes).")
    private String countryOfVaccination;
}
