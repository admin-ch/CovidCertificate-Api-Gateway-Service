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

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@RequiredArgsConstructor
public class CovidCertificateGenerationService {

    @Value("${cc-management-service.uri}")
    private String serviceUri;

    @Value("#{'${allowed-common-names-for-system-source}'.split(',')}")
    private List<String> allowedCommonNamesForSystemSource;

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

    public CovidCertificateCreateResponseDto createCovidCertificate(AntibodyCertificateCreateDto createDto) {
        return createCovidCertificate(createDto, "antibody");
    }

    private CovidCertificateCreateResponseDto createCovidCertificate(CertificateCreateDto createDto, String url) {
        final var uri = UriComponentsBuilder.fromHttpUrl(serviceUri + "api/v1/covidcertificate/" + url).toUriString();
        log.debug("Call the CovidCertificateGenerationService with url {}", kv("url", uri));

        if (createDto.getSystemSource() != null) {
            log.debug("SystemSource set in request. Checking CommonName...");
            var commonName = ((CustomHeaderAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
            if (allowedCommonNamesForSystemSource.contains(commonName) && SystemSource.ApiPlatform.equals(createDto.getSystemSource())) {
                log.debug("SystemSource set to ApiPlatform by {}", commonName);
            } else {
                createDto.setSystemSource(SystemSource.ApiGateway);
            }
        } else {
            createDto.setSystemSource(SystemSource.ApiGateway);
        }

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
