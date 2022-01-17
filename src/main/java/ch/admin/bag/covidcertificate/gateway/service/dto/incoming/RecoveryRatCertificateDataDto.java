package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecoveryRatCertificateDataDto {
    @Schema(example = "LP217198-3", description = "type of test. This field is optional if manufacturer code is present otherwise it must be LP217198-3 (RAPID-ANTIGEN-TEST).")
    private String typeCode;
    @Schema(example = "1232", description = "test manufacturer code. This should only be sent when it is not a PCR test, otherwise there will be a 400 BAD REQUEST.")
    private String manufacturerCode;
    @Schema(example = "2022-01-24T17:29:41Z", description = "date and time of the test sample collection, can't be before 2022-01-24. Format: ISO 8601 date incl. time.")
    private ZonedDateTime sampleDateTime;
    @Schema(example = "Walk-in-Lyss AG", description = "name of centre or facility. Format: string, maxLength: 80 CHAR.")
    private String testingCentreOrFacility;
    @Schema(example = "CH", description = "the country in which the covid certificate owner has been tested. Format: string (2 chars according to ISO 3166 Country Codes).")
    private String memberStateOfTest;
}