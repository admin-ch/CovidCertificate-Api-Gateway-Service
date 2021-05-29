package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RevocationDto {
    @Schema(example= "urn:uvci:01:CH:97DAB5E31B589AF3CAE2F53F", description = "UVCI of certificate to be revoked.")
    private String uvci;
    private String otp;
}