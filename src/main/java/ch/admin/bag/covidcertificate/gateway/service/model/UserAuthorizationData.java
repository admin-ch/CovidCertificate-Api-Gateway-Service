package ch.admin.bag.covidcertificate.gateway.service.model;

import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Role;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IdentityDto;
import lombok.Getter;

import java.util.List;

@Getter
public class UserAuthorizationData extends IdentityDto {
    private final List<String> roles;

    public UserAuthorizationData(String uuid, String idpSource, List<String> roles) {
        super(uuid, idpSource);
        this.roles = roles;
    }
}