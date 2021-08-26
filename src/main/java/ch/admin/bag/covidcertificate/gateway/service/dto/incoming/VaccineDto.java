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
    @Schema(type = "boolean", example = "true", description = "If vaccination is accepted by the BAG and the EU.")
    private Boolean active;

    public VaccineDto(String productCode, String productDisplay, String prophylaxisCode, String prophylaxisDisplay, String authHolderCode, String authHolderDisplay, Boolean active) {
        super(productCode, productDisplay, prophylaxisCode, prophylaxisDisplay, authHolderCode, authHolderDisplay);
        this.active = active;
    }
}
