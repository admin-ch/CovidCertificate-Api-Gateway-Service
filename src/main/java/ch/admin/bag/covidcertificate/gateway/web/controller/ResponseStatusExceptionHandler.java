package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.RevokeCertificateException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;
import java.util.stream.Stream;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;

@ControllerAdvice()
@ApiResponse(
        responseCode = "403",
        content = @Content(
                schema = @Schema(implementation = RestError.class),
                mediaType = "application/json",
                examples = {
                        @ExampleObject(name = "INVALID_BEARER", value = INVALID_BEARER_JSON),
                        @ExampleObject(name = "INVALID_SIGNATURE", value = INVALID_SIGNATURE_JSON),
                        @ExampleObject(name = "SIGNATURE_PARSE_ERROR", value = SIGNATURE_PARSE_JSON),
                        @ExampleObject(name = "INVALID_AUTHORIZATION_COMBINATION", value = INVALID_AUTHORIZATION_COMBINATION_JSON),
                        @ExampleObject(name = "INVALID_IDENTITY_USER", value = INVALID_IDENTITY_USER_JSON),
                        @ExampleObject(name = "INVALID_IDENTITY_USER_ROLE", value = INVALID_IDENTITY_USER_ROLE_JSON)
                })
)
@Slf4j
public class ResponseStatusExceptionHandler {

    @ExceptionHandler(value = {CreateCertificateException.class})
    protected ResponseEntity<RestError> createCertificateConflict(CreateCertificateException ex) {
        return handleError(ex.getError());
    }

    @ExceptionHandler(value = {RevokeCertificateException.class})
    protected ResponseEntity<RestError> createCertificateConflict(RevokeCertificateException ex) {
        return handleError(ex.getError());
    }

    @ExceptionHandler(value = {InvalidBearerTokenException.class})
    protected ResponseEntity<RestError> invalidBearer(InvalidBearerTokenException ex) {
        return handleError(ex.getError());
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleException(Exception e) {
        log.error("Exception during processing", e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    protected ResponseEntity<RestError> notReadableRequestPayload(HttpMessageNotReadableException ex) {
        var errorMessage = getRootCauseMessage(ex);
        var error = new RestError(HttpStatus.BAD_REQUEST.value(), errorMessage, HttpStatus.BAD_REQUEST);
        return handleError(error);
    }

    private String getRootCauseMessage(NestedRuntimeException ex){
        return Stream.of(ex.getRootCause(), ex.getCause(), ex)
                .filter(Objects::nonNull)
                .findFirst()
                .map(Throwable::getMessage)
                .orElse("");
    }
    
    private ResponseEntity<RestError> handleError(RestError restError) {
        log.error("Error {}", restError);
        return new ResponseEntity<>(restError, restError.getHttpStatus());
    }
}
