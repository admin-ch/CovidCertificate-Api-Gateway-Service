package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VaccinationCertificateCreateDto extends CertificateCreateDto {
    private List<VaccinationCertificateDataDto> vaccinationInfo;
}
