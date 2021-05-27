package ch.admin.bag.covidcertificate.gateway.service.dto;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import lombok.Getter;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class CreateCertificateException extends NestedRuntimeException {
    private final RestError error;

    public CreateCertificateException(RestError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
