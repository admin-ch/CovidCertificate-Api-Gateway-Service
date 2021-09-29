package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.*;
import ch.admin.bag.covidcertificate.gateway.service.util.WebClientUtils;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@RequiredArgsConstructor
public class CovidCertificateGenerationService {

    @Value("${cc-management-service.uri}")
    private String serviceUri;

    private final WebClient defaultWebClient;

    public CovidCertificateCreateResponseDto createCovidCertificate(TestCertificateCreateDto createDto) {
        return createCovidCertificate(createDto, "test");
    }

    public CovidCertificateCreateResponseDto createCovidCertificate(RecoveryCertificateCreateDto createDto) {
        return createCovidCertificate(createDto, "recovery");
    }

    public CovidCertificateCreateResponseDto createCovidCertificate(VaccinationCertificateCreateDto createDto) {
        return createCovidCertificate(createDto, "vaccination");
    }

    private CovidCertificateCreateResponseDto createCovidCertificate(CertificateCreateDto createDto, String url) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + "api/v1/covidcertificate/" + url);
        String uri = builder.toUriString();
        log.debug("Call the CovidCertificateGenerationService with url {}", kv("url", uri));

        //TODO: Set the systemSource to ApiPlatform if the commonName matches with the configured commonName in the properties
        //var commonName = ((CustomHeaderAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
        createDto.setSystemSource(SystemSource.ApiGateway);

        try {
            CovidCertificateCreateResponseDto response = defaultWebClient.post()
                    .uri(uri)
                    .body(Mono.just(createDto), createDto.getClass())
                    .retrieve()
                    .bodyToMono(CovidCertificateCreateResponseDto.class)
                    .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null")))
                    .block();

            log.trace("CovidCertificateGenerationService Response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new CreateCertificateException(errorResponse);
        }
    }
}
