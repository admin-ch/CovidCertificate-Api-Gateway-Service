package ch.admin.bag.covidcertificate.gateway.service.util;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public final class WebClientUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static RestError handleWebClientResponseError(WebClientResponseException e) {
        log.warn("Received error message: {}", e.getResponseBodyAsString());
        RestError errorResponse;
        try {
            var responseBody = e.getResponseBodyAsString();
            if (StringUtils.hasText(responseBody)) {
                errorResponse = mapper.readValue(e.getResponseBodyAsString(), RestError.class);
                errorResponse.setHttpStatus(e.getStatusCode());
                log.warn("Error response object: {} ", errorResponse);
            } else {
                errorResponse = new RestError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException ioException) {
            log.warn("Exception during parsing of error response", ioException);
            throw new IllegalStateException("Exception during parsing of error response", ioException);
        }

        return errorResponse;
    }

}
