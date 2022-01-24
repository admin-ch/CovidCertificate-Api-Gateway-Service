package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RevocationListDto implements DtoWithAuthorization {
    @Schema(example = "[urn:uvci:01:CH:97DAB5E31B589AF3CAE2F53F]", description = "Multiple UVCI of certificate to be checked or revoked.")
    private List<String> uvcis;
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
