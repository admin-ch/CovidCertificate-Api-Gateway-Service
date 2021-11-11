package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CountryCodeDto {
    @JsonProperty("short")
    private String shortName;
    private String display;
    private String lang;
    private boolean active;
    private String version;
    private String system;
}
