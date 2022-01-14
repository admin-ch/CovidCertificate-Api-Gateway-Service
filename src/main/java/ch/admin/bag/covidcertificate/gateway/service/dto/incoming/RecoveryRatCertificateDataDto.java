package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecoveryRatCertificateDataDto {
    @Schema(example = "1457", description = "Rapid-antigen test manufacturer code.")
    private String manufacturerCode;

    @Schema(example = "2022-01-24", description = "Date of the rapid-antigen test, this certificate is only valid for tests performed after 24.01.2022 midnight. Format: ISO 8601 date without time.")
    private LocalDate sampleDate;

    @Schema(example = "Walk-in-Lyss AG", description = "Name of the test center or facility. Format: string, maxLength: 80 CHAR.")
    private String testingCenterOrFacility;
}