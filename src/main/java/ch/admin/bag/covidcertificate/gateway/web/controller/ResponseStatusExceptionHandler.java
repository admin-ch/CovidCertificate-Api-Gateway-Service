package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.filters.PayloadLimitException;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;

@ControllerAdvice()
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "403",
                content = @Content(
                        schema = @Schema(implementation = RestError.class),
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(name = "INVALID_BEARER", value = INVALID_BEARER_JSON),
                                @ExampleObject(name = "INVALID_SIGNATURE", value = INVALID_SIGNATURE_JSON),
                                @ExampleObject(name = "SIGNATURE_PARSE_ERROR", value = SIGNATURE_PARSE_JSON)
                        })
        ),
        @ApiResponse(responseCode = "413",
                content = @Content(
                        schema = @Schema(implementation = RestError.class),
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(name = "PAYLOAD_TOO_LARGE", value = PAYLOAD_TOO_LARGE_JSON)
                        }
                )),
        @ApiResponse(responseCode = "500",
                content = @Content(
                        schema = @Schema(implementation = RestError.class),
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(name = "CREATE_COSE_PROTECTED_HEADER_FAILED", value = CREATE_COSE_PROTECTED_HEADER_FAILED),
                                @ExampleObject(name = "CREATE_COSE_PAYLOAD_FAILED", value = CREATE_COSE_PAYLOAD_FAILED),
                                @ExampleObject(name = "CREATE_COSE_SIGNATURE_DATA_FAILED", value = CREATE_COSE_SIGNATURE_DATA_FAILED),
                                @ExampleObject(name = "CREATE_SIGNATURE_FAILED", value = CREATE_SIGNATURE_FAILED),
                                @ExampleObject(name = "CREATE_COSE_SIGN1_FAILED", value = CREATE_COSE_SIGN1_FAILED),
                                @ExampleObject(name = "CREATE_BARCODE_FAILED", value = CREATE_BARCODE_FAILED)
                        }
                ))
})
public class ResponseStatusExceptionHandler {

    @ExceptionHandler(value = {CreateCertificateException.class})
    protected ResponseEntity<RestError> createCertificateConflict(CreateCertificateException ex) {
        return handleError(ex.getError());
    }

    @ExceptionHandler(value = {PayloadLimitException.class})
    protected ResponseEntity<RestError> bodyLimitReached(PayloadLimitException ex) {
        return handleError(ex.getError());
    }

    @ExceptionHandler(value = {InvalidBearerTokenException.class})
    protected ResponseEntity<RestError> invalidBearer(InvalidBearerTokenException ex) {
        return handleError(ex.getError());
    }

    private ResponseEntity<RestError> handleError(RestError restError) {
        return new ResponseEntity<>(restError, restError.getHttpStatus());
    }
}
