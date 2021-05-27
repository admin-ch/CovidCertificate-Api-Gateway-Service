package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import lombok.Getter;

@Getter
public class InvalidBearerTokenException extends Exception {
    private final RestError error;

    public InvalidBearerTokenException(RestError error) {
        this.error = error;
    }
}
