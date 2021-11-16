package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CountryCodesDto {
    private final List<CountryCodeDto> de;
    private final List<CountryCodeDto> en;
    private final List<CountryCodeDto> fr;
    private final List<CountryCodeDto> it;
    private final List<CountryCodeDto> rm;
}