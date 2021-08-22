package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VaccineDto extends IssuableVaccineDto {
    @Schema(type = "string")
    private String productCode;
    @Schema(type = "string")
    private String productDisplay;
    @Schema(type = "string")
    private String prophylaxisCode;
    @Schema(type = "string")
    private String prophylaxisDisplay;
    @Schema(type = "string")
    private String authHolderCode;
    @Schema(type = "string")
    private String authHolderDisplay;
    @Schema(type = "boolean")
    private Boolean active;

    public VaccineDto(String productCode, String productDisplay, String prophylaxisCode, String prophylaxisDisplay, String authHolderCode, String authHolderDisplay, Boolean active) {
        super(productCode, productDisplay, prophylaxisCode, prophylaxisDisplay, authHolderCode, authHolderDisplay);
        this.active = active;
    }
}
