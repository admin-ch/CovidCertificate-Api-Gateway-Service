package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.RevokeCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@RequiredArgsConstructor
public class CovidCertificateRevocationService {

    @Value("${ha-authcode-generation-service.uri}")
    private String serviceUri;

    private final WebClient defaultWebClient;

    private final ObjectMapper mapper = new ObjectMapper();

    public void createRevocation(RevocationDto revocationDto) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + "api/v1/revocation/");

        String uri = builder.toUriString();
        log.debug("Call the CovidCertificateRevocationService with url {}", kv("url", uri));
        try {
            defaultWebClient.post()
                    .uri(uri)
                    .body(Mono.just(revocationDto), revocationDto.getClass())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.warn("Received error message: {}", e.getResponseBodyAsString());
            RestError errorResponse;
            try {
                errorResponse = mapper.readValue(e.getResponseBodyAsString(), RestError.class);
                errorResponse.setHttpStatus(e.getStatusCode());
                log.warn("Error response object: {} ", errorResponse);

            } catch (IOException ioException) {
                log.warn("Exception during parsing of error response", ioException);
                throw new IllegalStateException("Exception during parsing of error response", ioException);
            }

            throw new RevokeCertificateException(errorResponse);
        }
    }
}
