package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.RevokeCertificateException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResponseStatusExceptionHandlerTest {
    private final JFixture fixture = new JFixture();

    private TestResponseStatusExceptionHandlerWrapper testExceptionHanlder = new TestResponseStatusExceptionHandlerWrapper();

    @Test
    public void createCertificateConflictReturnsRestError__withCreateCertificateException() {
        var exception = mock(CreateCertificateException.class);
        var restError = new RestError(400, "test", HttpStatus.BAD_REQUEST);
        when(exception.getError()).thenReturn(restError);

        var responseEntity = this.testExceptionHanlder.createCertificateConflict(exception);
        assertEquals(restError.getErrorCode(), Objects.requireNonNull(responseEntity.getBody()).getErrorCode());
        assertEquals(restError.getErrorMessage(), Objects.requireNonNull(responseEntity.getBody()).getErrorMessage());
        assertEquals(restError.getHttpStatus(), Objects.requireNonNull(responseEntity.getStatusCode()));
    }

    @Test
    public void createCertificateConflictReturnsRestError__withRevokeCertificateException() {
        var exception = mock(RevokeCertificateException.class);
        var restError = new RestError(400, "test", HttpStatus.BAD_REQUEST);
        when(exception.getError()).thenReturn(restError);

        var responseEntity = this.testExceptionHanlder.createCertificateConflict(exception);
        assertEquals(restError.getErrorCode(), Objects.requireNonNull(responseEntity.getBody()).getErrorCode());
        assertEquals(restError.getErrorMessage(), Objects.requireNonNull(responseEntity.getBody()).getErrorMessage());
        assertEquals(restError.getHttpStatus(), Objects.requireNonNull(responseEntity.getStatusCode()));
    }

    @Test
    public void invalidBearerReturnsRestError() {
        var exception = mock(InvalidBearerTokenException.class);
        var restError = new RestError(492, "test", HttpStatus.FORBIDDEN);
        when(exception.getError()).thenReturn(restError);

        var responseEntity = this.testExceptionHanlder.invalidBearer(exception);
        assertEquals(restError.getErrorCode(), Objects.requireNonNull(responseEntity.getBody()).getErrorCode());
        assertEquals(restError.getErrorMessage(), Objects.requireNonNull(responseEntity.getBody()).getErrorMessage());
        assertEquals(restError.getHttpStatus(), Objects.requireNonNull(responseEntity.getStatusCode()));
    }

    @Test
    public void notReadableHandlerReturnsInvalidValueMessage__ifInvalidFormatException() {
        final var testValue = "--VALUE--";
        var exception = mock(HttpMessageNotReadableException.class);
        var causedByException = new InvalidFormatException(mock(JsonParser.class), "", testValue, String.class);
        when(exception.getCause()).thenReturn(causedByException);

        var responseEntity = this.testExceptionHanlder.notReadableRequestPayload(exception);
        assertEquals(400, Objects.requireNonNull(responseEntity.getBody()).getErrorCode());
        var expectedErrorMessage = "Unable to parse " + testValue + " to " + String.class;
        assertEquals(expectedErrorMessage, responseEntity.getBody().getErrorMessage());
    }

    @Test
    public void notReadableHandlerReturnsUnreadableMessage__ifNotInvalidFormatException() {
        var exception = mock(HttpMessageNotReadableException.class);
        when(exception.getCause()).thenReturn(new RuntimeException());

        var responseEntity = this.testExceptionHanlder.notReadableRequestPayload(exception);
        assertEquals(400, Objects.requireNonNull(responseEntity.getBody()).getErrorCode());
        var expectedErrorMessage = "Http message not readable";
        assertEquals(expectedErrorMessage, responseEntity.getBody().getErrorMessage());
    }

    @Test
    public void returns500__onAnyException() {
        var exception = mock(Exception.class);
        var responseEntity = this.testExceptionHanlder.handleException(exception);
        assertEquals(responseEntity.getStatusCode(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static class TestResponseStatusExceptionHandlerWrapper {
        private final ResponseStatusExceptionHandler responseStatusExceptionHandler = new ResponseStatusExceptionHandler();

        public ResponseEntity<RestError> createCertificateConflict(CreateCertificateException ex) {
            return responseStatusExceptionHandler.createCertificateConflict(ex);
        }

        public ResponseEntity<RestError> createCertificateConflict(RevokeCertificateException ex) {
            return responseStatusExceptionHandler.createCertificateConflict(ex);
        }

        public ResponseEntity<RestError> invalidBearer(InvalidBearerTokenException ex) {
            return responseStatusExceptionHandler.invalidBearer(ex);
        }

        public ResponseEntity<RestError> notReadableRequestPayload(HttpMessageNotReadableException ex) {
            return responseStatusExceptionHandler.notReadableRequestPayload(ex);
        }

        public ResponseEntity<Object> handleException(Exception e) {
            return responseStatusExceptionHandler.handleException(e);
        }
    }

}