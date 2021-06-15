package ch.admin.bag.covidcertificate.gateway.service.dto.incoming;

public interface DtoWithAuthorization {
    IdentityDto getIdentity();
    String getOtp();
}
