package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AntibodyCertificateDataDto {
    @Schema(example = "2021-10-03", description = "date when the sample for the test was collected that led to serology positive test obtained through a procedure established by a public health authority. Format: ISO 8601 date without time.")
    private LocalDate sampleDate;
    @Schema(example = "512345-123456789, SwissLabTest Center Zürich", description = " the Swissmedic authorization number (mandatory) of the laboratory + name of the laboratory (optional).", type = "string", maxLength = 80)
    private String testingCenterOrFacility;
}
