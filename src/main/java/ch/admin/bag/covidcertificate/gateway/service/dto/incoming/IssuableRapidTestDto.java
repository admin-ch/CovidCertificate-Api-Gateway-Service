package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IssuableRapidTestDto {
    @Schema(type = "string", example = "1232", description = "Code of rapid test as string.")
    private String code;
    @Schema(type = "string", example = "Abbott Rapid Diagnostics, Panbio Covid-19 Ag Rapid Test", description = "Manufacturer and display name of rapid test as string.")
    private String display;
}
