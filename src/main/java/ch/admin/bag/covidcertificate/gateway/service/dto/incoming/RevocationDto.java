package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RevocationDto implements DtoWithAuthorization {
    @Schema(example = "urn:uvci:01:CH:97DAB5E31B589AF3CAE2F53F", description = "UVCI of certificate to be revoked.")
    private String uvci;
    private boolean fraud;
    private String otp;
    @Schema(hidden = true)
    private IdentityDto identity;
    @Schema(hidden = true)
    @Setter
    private SystemSource systemSource;
    @Schema(hidden = true)
    @Setter
    private String userExtId;
}
