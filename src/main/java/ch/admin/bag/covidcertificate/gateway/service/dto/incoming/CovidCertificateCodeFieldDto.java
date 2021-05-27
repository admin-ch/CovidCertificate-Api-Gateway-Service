package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CovidCertificateCodeFieldDto {
    private String code;
}
