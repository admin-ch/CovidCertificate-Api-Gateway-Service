package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.CheckRevocationListResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.RevocationListResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.RevokeCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationListDto;
import ch.admin.bag.covidcertificate.gateway.service.util.WebClientUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@RequiredArgsConstructor
public class CovidCertificateRevocationService {

    @Value("${cc-management-service.uri}")
    private String serviceUri;

    private final WebClient defaultWebClient;

    private final SystemSourceService systemSourceService;

    public void createRevocation(RevocationDto revocationDto, String userExtId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + "api/v1/revocation/");

        revocationDto.setSystemSource(systemSourceService.getRelevantSystemSource(revocationDto.getSystemSource()));
        revocationDto.setUserExtId(userExtId);

        String uri = builder.toUriString();
        log.debug("Call the CovidCertificateRevocationService with {}", kv("url", uri));
        try {
            defaultWebClient.post()
                    .uri(uri)
                    .body(Mono.just(revocationDto), revocationDto.getClass())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new RevokeCertificateException(errorResponse);
        }
    }

    public RevocationListResponseDto createMassRevocation(RevocationListDto revocationListDto, String userExtId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + "api/v1/revocation/uvcilist");

        revocationListDto.setSystemSource(systemSourceService.getRelevantSystemSource(revocationListDto.getSystemSource()));
        revocationListDto.setUserExtId(userExtId);
        String uri = builder.toUriString();

        log.debug("Call the CovidCertificateRevocationService with {}", kv("url", uri));
        try {
            RevocationListResponseDto response = defaultWebClient.post()
                    .uri(uri)
                    .body(Mono.just(revocationListDto), revocationListDto.getClass())
                    .retrieve()
                    .bodyToMono(RevocationListResponseDto.class)
                    .block();

            log.trace("CovidCertificateGenerationService Response: {}", response);
            return response;
        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new RevokeCertificateException(errorResponse);
        }
    }
}
