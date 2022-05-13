package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UvciForRevocationDto {
    @Schema(description = "UVCI of certificate to be revoked.")
    private String uvci;
    @Schema(description = "Flag to indicate if certificate is revoked due to fraud reasons.")
    private Boolean fraud;
}