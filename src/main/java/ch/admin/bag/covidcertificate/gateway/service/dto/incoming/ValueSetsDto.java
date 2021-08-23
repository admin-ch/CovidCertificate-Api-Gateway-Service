package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ValueSetsDto implements DtoWithAuthorization {
    private String otp;
    @Schema(hidden = true)
    private IdentityDto identity;
}
