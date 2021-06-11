package ch.admin.bag.covidcertificate.gateway.service.util;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebClientUtilsTest {

    static final JFixture fixture = new JFixture();
    static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsWebClientExceptionBody__ifOk() throws Exception {
        var restErrorMock = fixture.create(RestError.class);
        var webClientException = mock(WebClientResponseException.class);
        when(webClientException.getResponseBodyAsString()).thenReturn(objectMapper.writeValueAsString(restErrorMock));
        when(webClientException.getStatusCode()).thenReturn(HttpStatus.OK);

        var result = WebClientUtils.handleWebClientResponseError(webClientException);
        assertEquals(restErrorMock.getErrorMessage(), result.getErrorMessage());
    }

    @Test
    void throwsIllegalArgumentException__ifInvalidBody() {
        var invalidBody = "invalid body";
        var webClientException = mock(WebClientResponseException.class);
        when(webClientException.getResponseBodyAsString()).thenReturn(invalidBody);
        when(webClientException.getStatusCode()).thenReturn(HttpStatus.OK);

        assertThrows(IllegalStateException.class, () -> WebClientUtils.handleWebClientResponseError(webClientException));
    }

}