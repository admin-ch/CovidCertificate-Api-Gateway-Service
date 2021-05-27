package ch.admin.bag.covidcertificate.gateway.filters;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import lombok.Getter;

@Getter
public class PayloadLimitException extends RuntimeException {
    private final RestError error;

    public PayloadLimitException(RestError error) {
        this.error = error;
    }
}
