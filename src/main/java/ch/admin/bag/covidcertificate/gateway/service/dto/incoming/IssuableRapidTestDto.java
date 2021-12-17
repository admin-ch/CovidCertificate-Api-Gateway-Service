package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IssuableRapidTestDto {
    @Schema(type = "string", example = "1232", description = "Code of rapid test as string.")
    private String code;
    @Schema(type = "string", example = "Abbott Rapid Diagnostics, Panbio Covid-19 Ag Rapid Test", description = "Manufacturer and display name of rapid test as string.")
    private String display;
    @Schema(type = "string", format = "yyyy-MM-dd HH:mm:ss z", example = "2022-01-06T00:00:00+01:00", description = "Deadline after which the rapid-test can no longer be used to establish a certificate.")
    private ZonedDateTime validUntil;
}
