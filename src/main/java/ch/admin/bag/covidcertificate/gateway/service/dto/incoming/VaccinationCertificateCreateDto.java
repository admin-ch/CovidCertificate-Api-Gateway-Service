package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import lombok.*;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_VACCINATION_INFO;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VaccinationCertificateCreateDto extends CertificateCreateDto {
    private List<VaccinationCertificateDataDto> vaccinationInfo;

    public void validate() {
        if (vaccinationInfo == null || vaccinationInfo.size() != 1) {
            throw new CreateCertificateException(INVALID_VACCINATION_INFO);
        }
    }
}
