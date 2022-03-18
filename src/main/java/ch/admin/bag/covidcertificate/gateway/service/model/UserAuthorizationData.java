package ch.admin.bag.covidcertificate.gateway.service.model;

import lombok.Data;

import java.util.List;

@Data
public class UserAuthorizationData {
    private final String userId;
    private final String idpSource;
    private final List<String> roles;
}