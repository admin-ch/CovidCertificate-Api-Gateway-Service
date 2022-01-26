package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.domain.Issuable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IssuableVaccineDto {
    @Schema(type = "string", example = "EU/1/20/1507", description = "Code of vaccination as string.")
    private String productCode;
    @Schema(type = "string", example = "Spikevax (previously COVID-19 Vaccine Moderna)", description = "Display name of vaccination as string.")
    private String productDisplay;
    @Schema(type = "string", example = "1119349007", description = "Code of prophylaxis type as string.")
    private String prophylaxisCode;
    @Schema(type = "string", example = "SARS-CoV-2 mRNA vaccine", description = "Display name of prophylaxis type as string.")
    private String prophylaxisDisplay;
    @Schema(type = "string", example = "ORG-100031184", description = "Code of authorization holder as string.")
    private String authHolderCode;
    @Schema(type = "string", example = "Moderna Biotech Spain S.L.", description = "Display name of authorization holder as string.")
    private String authHolderDisplay;
    @Schema(type = "string", example = "CH_AND_ABROAD", description = "This attribute tells us if a vaccine is issuable in CH_ONLY, CH_AND_ABROAD or ABROAD_ONLY.")
    private Issuable issuable;
}