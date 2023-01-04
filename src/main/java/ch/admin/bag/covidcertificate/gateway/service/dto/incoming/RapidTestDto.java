package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RapidTestDto extends IssuableRapidTestDto {
    @Schema(type = "boolean", example = "true", description = "If rapid test is accepted by the BAG and the EU.")
    private Boolean active;
}
