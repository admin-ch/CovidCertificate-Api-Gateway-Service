package ch.admin.bag.covidcertificate.gateway.service.model;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Objects;

@Data
public class UserAuthorizationData {
    private final String userId;
    private final String idpSource;
    private final List<String> roles;

    public boolean isValid() {
        return Strings.isNotBlank(userId)
                && Strings.isNotBlank(idpSource)
                && !Objects.isNull(roles);
    }
}