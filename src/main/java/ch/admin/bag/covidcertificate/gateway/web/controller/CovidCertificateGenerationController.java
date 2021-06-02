package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.BearerTokenValidationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateGenerationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.KpiDataService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CovidCertificateCreateResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.TestCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationCertificateCreateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static ch.admin.bag.covidcertificate.gateway.Constants.*;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/covidcertificate")
@RequiredArgsConstructor
public class CovidCertificateGenerationController {

    private final CovidCertificateGenerationService generationService;

    private final BearerTokenValidationService bearerTokenValidationService;

    private final KpiDataService kpiDataService;

    @PostMapping("/vaccination")
    @Operation(operationId = "createVaccinationCertificate",
            summary = "Creates a vaccine certificate for the given data.",
            description = "Creates a vaccine certificate as an QR-Code and PDF. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CovidCertificateCreateResponseDto.class)))
    @ApiResponse(responseCode = "400",
            content = @Content(
                    schema = @Schema(implementation = RestError.class),
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "NO_VACCINATION_DATA", value = NO_VACCINATION_DATA),
                            @ExampleObject(name = "NO_PERSON_DATA", value = NO_PERSON_DATA),
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH", value = INVALID_DATE_OF_BIRTH),
                            @ExampleObject(name = "INVALID_MEDICINAL_PRODUCT", value = INVALID_MEDICINAL_PRODUCT),
                            @ExampleObject(name = "INVALID_DOSES", value = INVALID_DOSES),
                            @ExampleObject(name = "INVALID_VACCINATION_DATE", value = INVALID_VACCINATION_DATE),
                            @ExampleObject(name = "INVALID_COUNTRY_OF_VACCINATION", value = INVALID_COUNTRY_OF_VACCINATION),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_COUNTRY_SHORT_FORM", value = INVALID_COUNTRY_SHORT_FORM),
                            @ExampleObject(name = "INVALID_LANGUAGE", value = INVALID_LANGUAGE),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody VaccinationCertificateCreateDto createDto) throws InvalidBearerTokenException {
        log.info("Call of Create for vaccination certificate");
        String userExtId = bearerTokenValidationService.validate(createDto.getOtp());

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logKpi(KPI_TYPE_VACCINATION, userExtId);
        return covidCertificate;

    }

    @PostMapping("/test")
    @Operation(operationId = "createTestCertificate",
            summary = "Creates a test certificate for the given data.",
            description = "Creates a test certificate as an QR-Code and PDF. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CovidCertificateCreateResponseDto.class)))
    @ApiResponse(responseCode = "400",
            content = @Content(
                    schema = @Schema(implementation = RestError.class),
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "NO_PERSON_DATA", value = NO_PERSON_DATA),
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH", value = INVALID_DATE_OF_BIRTH),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "NO_TEST_DATA", value = NO_TEST_DATA),
                            @ExampleObject(name = "INVALID_MEMBER_STATE_OF_TEST", value = INVALID_MEMBER_STATE_OF_TEST),
                            @ExampleObject(name = "INVALID_TYP_OF_TEST", value = INVALID_TYP_OF_TEST),
                            @ExampleObject(name = "INVALID_TEST_CENTER", value = INVALID_TEST_CENTER),
                            @ExampleObject(name = "INVALID_SAMPLE_OR_RESULT_DATE_TIME", value = INVALID_SAMPLE_OR_RESULT_DATE_TIME),
                            @ExampleObject(name = "INVALID_COUNTRY_SHORT_FORM", value = INVALID_COUNTRY_SHORT_FORM),
                            @ExampleObject(name = "INVALID_LANGUAGE", value = INVALID_LANGUAGE),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody TestCertificateCreateDto createDto) throws InvalidBearerTokenException {
        log.info("Call of Create for test certificate");
        String userExtId = bearerTokenValidationService.validate(createDto.getOtp());

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logKpi(KPI_TYPE_TEST, userExtId);
        return covidCertificate;
    }

    @PostMapping("/recovery")
    @Operation(operationId = "createRecoveryCertificate",
            summary = "Creates a recovery certificate for the given data.",
            description = "Creates a recovery certificate as an QR-Code and PDF. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            })
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CovidCertificateCreateResponseDto.class)))
    @ApiResponse(responseCode = "400",
            content = @Content(
                    schema = @Schema(implementation = RestError.class),
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "NO_PERSON_DATA", value = NO_PERSON_DATA),
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH", value = INVALID_DATE_OF_BIRTH),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "NO_RECOVERY_DATA", value = NO_RECOVERY_DATA),
                            @ExampleObject(name = "INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT", value = INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT),
                            @ExampleObject(name = "INVALID_COUNTRY_OF_TEST", value = INVALID_COUNTRY_OF_TEST),
                            @ExampleObject(name = "INVALID_COUNTRY_SHORT_FORM", value = INVALID_COUNTRY_SHORT_FORM),
                            @ExampleObject(name = "INVALID_LANGUAGE", value = INVALID_LANGUAGE),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody RecoveryCertificateCreateDto createDto) throws InvalidBearerTokenException {
        log.info("Call of Create for recovery certificate");
        String userExtId = bearerTokenValidationService.validate(createDto.getOtp());

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logKpi(KPI_TYPE_RECOVERY, userExtId);
        return covidCertificate;
    }

    private void logKpi(String type, String userExtId) {
        LocalDateTime timestamp = LocalDateTime.now();
        log.info("kpi: {} {} {} {}", kv(KPI_TIMESTAMP_KEY, timestamp.format(LOG_FORMAT)), kv(KPI_CREATE_CERTIFICATE_TYPE, KPI_SYSTEM_API), kv(KPI_TYPE_KEY, type), kv(KPI_UUID_KEY, userExtId));
        kpiDataService.saveKpiData(timestamp, type, userExtId);
    }
}
