package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class IdentityDto {
    private String uuid;
    private String idpSource;
}
