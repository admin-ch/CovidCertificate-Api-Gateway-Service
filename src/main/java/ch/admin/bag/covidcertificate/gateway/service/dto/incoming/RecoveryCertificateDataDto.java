package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecoveryCertificateDataDto {
    @Schema(example = "2021-10-03", description = "date when the sample for the test was collected that led to positive test obtained through a procedure established by a public health authority. Format: ISO 8601 date without time. Range: can be between 1900-01-01 and 2099-12-31. Regexp: \"[19|20][0-9][0-9]-(0[1-9]|1[0-2])-([0-2][1-9]|3[0|1])\".")
    private LocalDate dateOfFirstPositiveTestResult;
    @Schema(example = "CH", description = "the country in which the covid certificate owner has been tested. Format: string (2 chars according to ISO 3166 Country Codes).")
    private String countryOfTest;
}
