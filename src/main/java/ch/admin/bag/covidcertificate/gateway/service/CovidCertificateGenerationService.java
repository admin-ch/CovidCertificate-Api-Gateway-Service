package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.AntibodyCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CovidCertificateCreateResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryRatCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.TestCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationTouristCertificateCreateDto;
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
public class CovidCertificateGenerationService {

    @Value("${cc-management-service.uri}")
    private String serviceURL;

    @Value("${cc-management-service.covidcertificate.api.v1-path}")
    private String covidcertificateApiV1Path;

    private final WebClient defaultWebClient;

    private final SystemSourceService systemSourceService;

    public CovidCertificateCreateResponseDto createCovidCertificate(TestCertificateCreateDto createDto, String userExtId) {
        return createCovidCertificate(createDto, "test", userExtId);
    }

    public CovidCertificateCreateResponseDto createCovidCertificate(RecoveryCertificateCreateDto createDto, String userExtId) {
        return createCovidCertificate(createDto, "recovery", userExtId);
    }

    public CovidCertificateCreateResponseDto createCovidCertificate(RecoveryRatCertificateCreateDto createDto, String userExtId) {
        return createCovidCertificate(createDto, "recovery-rat", userExtId);
    }

    public CovidCertificateCreateResponseDto createCovidCertificate(VaccinationCertificateCreateDto createDto, String userExtId) {
        return createCovidCertificate(createDto, "vaccination", userExtId);
    }

    public CovidCertificateCreateResponseDto createCovidCertificate(VaccinationTouristCertificateCreateDto createDto, String userExtId) {
        return createCovidCertificate(createDto, "vaccination-tourist", userExtId);
    }

    public CovidCertificateCreateResponseDto createCovidCertificate(AntibodyCertificateCreateDto createDto, String userExtId) {
        return createCovidCertificate(createDto, "antibody", userExtId);
    }

    private CovidCertificateCreateResponseDto createCovidCertificate(CertificateCreateDto createDto, String resourcePath, String userExtId) {
        final var uri = UriComponentsBuilder.fromHttpUrl(serviceURL + covidcertificateApiV1Path + resourcePath).toUriString();
        log.debug("Call the CovidCertificateGenerationService with {}", kv("url", uri));

        createDto.setSystemSource(systemSourceService.getRelevantSystemSource(createDto.getSystemSource()));
        createDto.setUserExtId(userExtId);

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
