package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import lombok.*;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_ANTIBODY_INFO;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_RECOVERY_INFO;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AntibodyCertificateCreateDto extends CertificateCreateDto {
    private List<AntibodyCertificateDataDto> antibodyInfo;

    public void validate() {
        if (antibodyInfo == null || antibodyInfo.size() != 1) {
            throw new CreateCertificateException(INVALID_ANTIBODY_INFO);
        }
    }
}
