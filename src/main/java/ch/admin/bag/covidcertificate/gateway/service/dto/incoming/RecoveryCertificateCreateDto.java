package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import lombok.*;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_RECOVERY_INFO;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecoveryCertificateCreateDto extends CertificateCreateDto {
    private List<RecoveryCertificateDataDto> recoveryInfo;

    public void validate() {
        if (recoveryInfo == null || recoveryInfo.size() != 1) {
            throw new CreateCertificateException(INVALID_RECOVERY_INFO);
        }
    }
}
