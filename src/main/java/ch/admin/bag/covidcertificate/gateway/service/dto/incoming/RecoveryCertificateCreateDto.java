package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecoveryCertificateCreateDto extends CertificateCreateDto {
    private List<RecoveryCertificateDataDto> recoveryInfo;
}
