package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.service.dto.ReadValueSetsException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CountryCodeDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CountryCodesDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableRapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableVaccineDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.SystemSource;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccineDto;
import ch.admin.bag.covidcertificate.gateway.service.util.WebClientUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
public class ValueSetsService {

    public static final String RAPID_TESTS_PATH = "api/v1/valuesets/rapid-tests";
    public static final String ISSUABLE_RAPID_TESTS_PATH = "api/v1/valuesets/issuable-rapid-tests";
    public static final String VACCINES_PATH = "api/v1/valuesets/vaccines";
    public static final String ISSUABLE_VACCINES_PATH = "api/v1/valuesets/issuable-vaccines";
    public static final String COUNTRY_CODE_PATH = "api/v1/valuesets/countries";
    public static final String PARAMETER_SYSTEM_SOURCE = "systemSource";

    @Value("${cc-management-service.uri}")
    private String serviceUri;

    private final WebClient defaultWebClient;

    public List<RapidTestDto> getRapidTests() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + RAPID_TESTS_PATH);

        String uri = builder.toUriString();
        log.debug("Call the ValueSetsService with url {}", kv("url", uri));
        try {
            List<RapidTestDto> response = defaultWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<RapidTestDto>>() {})
                    .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null")))
                    .block();

            log.trace("ValueSetsService Response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new ReadValueSetsException(errorResponse);
        }
    }

    public List<IssuableRapidTestDto> getIssuableRapidTests() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + ISSUABLE_RAPID_TESTS_PATH);

        String uri = builder.toUriString();
        log.debug("Call the ValueSetsService with url {}", kv("url", uri));
        try {
            List<IssuableRapidTestDto> response = defaultWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<IssuableRapidTestDto>>() {})
                    .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null")))
                    .block();

            log.trace("ValueSetsService Response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new ReadValueSetsException(errorResponse);
        }
    }

    public List<VaccineDto> getVaccines() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + VACCINES_PATH);

        String uri = builder.toUriString();
        log.debug("Call the ValueSetsService with url {}", kv("url", uri));
        try {
            List<VaccineDto> response = defaultWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<VaccineDto>>() {})
                    .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null")))
                    .block();

            log.trace("ValueSetsService Response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new ReadValueSetsException(errorResponse);
        }
    }

    public List<IssuableVaccineDto> getIssuableVaccines(SystemSource systemSource) {
        UriComponentsBuilder builder;
        if(systemSource != null) {
            builder = UriComponentsBuilder.fromHttpUrl(serviceUri + ISSUABLE_VACCINES_PATH + "/" + systemSource.name());
        } else {
            builder = UriComponentsBuilder.fromHttpUrl(serviceUri + ISSUABLE_VACCINES_PATH);
        }

        String uri = builder.toUriString();
        log.debug("Call the ValueSetsService with url {}", kv("url", uri));
        try {
            List<IssuableVaccineDto> response = defaultWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<IssuableVaccineDto>>() {})
                    .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null")))
                    .block();

            log.trace("ValueSetsService Response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new ReadValueSetsException(errorResponse);
        }
    }

    public CountryCodesDto getCountryCodes(){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + COUNTRY_CODE_PATH);

        String uri = builder.toUriString();
        log.debug("Call the ValueSetsService with url {}", kv("url", uri));
        try {
            CountryCodesDto response = defaultWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<CountryCodesDto>() {})
                    .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null")))
                    .block();

            log.trace("ValueSetsService Response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new ReadValueSetsException(errorResponse);
        }
    }

    public List<CountryCodeDto> getCountryCodesByLanguage(String language){

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceUri + COUNTRY_CODE_PATH + "/" + language);

        String uri = builder.toUriString();
        log.debug("Call the ValueSetsService with url {}", kv("url", uri));
        try {
            List<CountryCodeDto> response = defaultWebClient
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CountryCodeDto>>() {})
                    .switchIfEmpty(Mono.error(new IllegalStateException("Response Body is null")))
                    .block();

            log.trace("ValueSetsService Response: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            RestError errorResponse = WebClientUtils.handleWebClientResponseError(e);
            throw new ReadValueSetsException(errorResponse);
        }
    }
}
