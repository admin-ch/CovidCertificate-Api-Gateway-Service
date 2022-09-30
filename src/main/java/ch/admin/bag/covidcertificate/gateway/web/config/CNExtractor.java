package ch.admin.bag.covidcertificate.gateway.web.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CNExtractor {

    public static String extract(String subject) {
        return StringUtils.substringAfter(subject, "CN=");
    }

}
