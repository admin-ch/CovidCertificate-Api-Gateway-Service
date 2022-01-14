package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.MISSING_RECOVERY_RAT_INFO;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RecoveryRatCertificateCreateDto extends CertificateCreateDto {
    @Schema(hidden = true)
    private CovidCertificateAddressDto address;
    private List<RecoveryRatCertificateDataDto> recoveryRatInfo ;

    public void validate() {
        if (recoveryRatInfo == null || recoveryRatInfo.size() != 1) {
            throw new CreateCertificateException(MISSING_RECOVERY_RAT_INFO);
        }
    }
}
