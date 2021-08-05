package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_TEST_INFO;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TestCertificateCreateDto extends CertificateCreateDto {
    @Schema(hidden = true)
    private CovidCertificateAddressDto address;
    private List<TestCertificateDataDto> testInfo;

    public void validate() {
        if (testInfo == null || testInfo.size() != 1) {
            throw new CreateCertificateException(INVALID_TEST_INFO);
        }
    }
}
