package ch.admin.bag.covidcertificate.gateway.service.dto;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

@Getter
public class ReadValueSetsException extends NestedRuntimeException {
    private final RestError error;

    public ReadValueSetsException(RestError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
