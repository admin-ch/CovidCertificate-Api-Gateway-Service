package ch.admin.bag.covidcertificate.gateway.service;

import lombok.Getter;

@Getter
public class APICallException extends Exception {

    private final String status;

    public APICallException(String status,String message) {
        super(message);
        this.status = status;
    }
}
