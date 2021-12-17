package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_VACCINATION_INFO;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VaccinationTouristCertificateCreateDto extends CertificateCreateDto {
    private List<VaccinationTouristCertificateDataDto> vaccinationTouristInfo;

    public void validate() {
        if (vaccinationTouristInfo == null || vaccinationTouristInfo.size() != 1) {
            throw new CreateCertificateException(INVALID_VACCINATION_INFO);
        }
    }
}
