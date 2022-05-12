package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RevocationListDto implements DtoWithAuthorization {
    @Schema(description = "Multiple UVCI with fraud flag of certificate to be revoked.")
    private List<UvciForRevocationDto> uvcis;
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
