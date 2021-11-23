package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.Constants;
import ch.admin.bag.covidcertificate.gateway.domain.TestType;
import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateGenerationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.KpiDataService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static ch.admin.bag.covidcertificate.gateway.Constants.*;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/covidcertificate")
@RequiredArgsConstructor
@ApiResponse(
        responseCode = "403",
        content = @Content(
                schema = @Schema(implementation = RestError.class),
                mediaType = "application/json",
                examples = {
                        @ExampleObject(name = "INVALID_BEARER", value = INVALID_BEARER_JSON),
                        @ExampleObject(name = "MISSING_BEARER_JSON", value = MISSING_BEARER_JSON),
                        @ExampleObject(name = "INVALID_SIGNATURE", value = INVALID_SIGNATURE_JSON),
                        @ExampleObject(name = "SIGNATURE_PARSE_ERROR", value = SIGNATURE_PARSE_JSON),
                        @ExampleObject(name = "INVALID_IDENTITY_USER", value = INVALID_IDENTITY_USER_JSON),
                        @ExampleObject(name = "INVALID_IDENTITY_USER_ROLE", value = INVALID_IDENTITY_USER_ROLE_JSON),
                        @ExampleObject(name = "INVALID_OTP_LENGTH", value = INVALID_OTP_LENGTH_JSON),
                })
)
public class CovidCertificateGenerationController {

    private final CovidCertificateGenerationService generationService;
    private final AuthorizationService authorizationService;
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
                            @ExampleObject(name = "NO_PERSON_DATA", value = NO_PERSON_DATA),
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH", value = INVALID_DATE_OF_BIRTH),
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH_IN_FUTURE", value = INVALID_DATE_OF_BIRTH_IN_FUTURE),
                            @ExampleObject(name = "INVALID_MEDICINAL_PRODUCT", value = INVALID_MEDICINAL_PRODUCT),
                            @ExampleObject(name = "INVALID_DOSES", value = INVALID_DOSES),
                            @ExampleObject(name = "INVALID_VACCINATION_DATE", value = INVALID_VACCINATION_DATE),
                            @ExampleObject(name = "INVALID_COUNTRY_OF_VACCINATION", value = INVALID_COUNTRY_OF_VACCINATION),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_GIVEN_NAME", value = INVALID_STANDARDISED_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_FAMILY_NAME", value = INVALID_STANDARDISED_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_VACCINATION_INFO", value = INVALID_VACCINATION_INFO_JSON),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                            @ExampleObject(name = "DUPLICATE_DELIVERY_METHOD", value = DUPLICATE_DELIVERY_METHOD),
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody VaccinationCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for vaccination certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr());
        createDto.validate();

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logKpi(KPI_TYPE_VACCINATION, userExtId, createDto, covidCertificate.getUvci(), createDto.getVaccinationInfo().get(0).getMedicinalProductCode(), createDto.getVaccinationInfo().get(0).getCountryOfVaccination());
        if (createDto.getVaccinationInfo().get(0).getNumberOfDoses()==1 &&
                createDto.getVaccinationInfo().get(0).getTotalNumberOfDoses()==1) {
            log.info("fraud: {}", kv("risk", "1/1"));
        }
        return covidCertificate;
    }

    @PostMapping("/vaccination-tourist")
    @Operation(operationId = "createVaccinationTouristCertificate",
            summary = "Creates a WHO vaccination-tourist certificate for the given data.",
            description = "Creates a WHO vaccination-tourist certificate as an QR-Code and PDF. Performs an integrity check for each request based on headers and body.",
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
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH_IN_FUTURE", value = INVALID_DATE_OF_BIRTH_IN_FUTURE),
                            @ExampleObject(name = "INVALID_MEDICINAL_PRODUCT", value = INVALID_MEDICINAL_PRODUCT),
                            @ExampleObject(name = "INVALID_DOSES", value = INVALID_DOSES),
                            @ExampleObject(name = "INVALID_VACCINATION_DATE", value = INVALID_VACCINATION_DATE),
                            @ExampleObject(name = "INVALID_COUNTRY_OF_VACCINATION", value = INVALID_COUNTRY_OF_VACCINATION),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_GIVEN_NAME", value = INVALID_STANDARDISED_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_FAMILY_NAME", value = INVALID_STANDARDISED_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_VACCINATION_INFO", value = INVALID_VACCINATION_INFO_JSON),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                            @ExampleObject(name = "DUPLICATE_DELIVERY_METHOD", value = DUPLICATE_DELIVERY_METHOD),
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody VaccinationTouristCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for WHO vaccination-tourist certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr());
        createDto.validate();

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logKpi(KPI_TYPE_VACCINATION, userExtId, createDto, covidCertificate.getUvci(), createDto.getVaccinationTouristInfo().get(0).getMedicinalProductCode(), createDto.getVaccinationTouristInfo().get(0).getCountryOfVaccination());
        if (createDto.getVaccinationTouristInfo().get(0).getNumberOfDoses()==1 &&
                createDto.getVaccinationTouristInfo().get(0).getTotalNumberOfDoses()==1) {
            log.info("fraud: {}", kv("risk", "1/1"));
        }
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
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH_IN_FUTURE", value = INVALID_DATE_OF_BIRTH_IN_FUTURE),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_GIVEN_NAME", value = INVALID_STANDARDISED_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_FAMILY_NAME", value = INVALID_STANDARDISED_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_MEMBER_STATE_OF_TEST", value = INVALID_MEMBER_STATE_OF_TEST),
                            @ExampleObject(name = "INVALID_TYP_OF_TEST", value = INVALID_TYP_OF_TEST),
                            @ExampleObject(name = "INVALID_TEST_CENTER", value = INVALID_TEST_CENTER),
                            @ExampleObject(name = "INVALID_SAMPLE_OR_RESULT_DATE_TIME", value = INVALID_SAMPLE_OR_RESULT_DATE_TIME),
                            @ExampleObject(name = "INVALID_LANGUAGE", value = INVALID_LANGUAGE),
                            @ExampleObject(name = "INVALID_TEST_INFO", value = INVALID_TEST_INFO_JSON),
                            @ExampleObject(name = "DUPLICATE_DELIVERY_METHOD", value = DUPLICATE_DELIVERY_METHOD),
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE),
                            @ExampleObject(name = "INVALID_PRINT_FOR_TEST", value = INVALID_PRINT_FOR_TEST),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody TestCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for test certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr());
        createDto.validate();

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logTestCertificateGenerationKpi(createDto, userExtId, covidCertificate.getUvci());
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
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH_IN_FUTURE", value = INVALID_DATE_OF_BIRTH_IN_FUTURE),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_GIVEN_NAME", value = INVALID_STANDARDISED_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_FAMILY_NAME", value = INVALID_STANDARDISED_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT", value = INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT),
                            @ExampleObject(name = "INVALID_COUNTRY_OF_TEST", value = INVALID_COUNTRY_OF_TEST),
                            @ExampleObject(name = "INVALID_LANGUAGE", value = INVALID_LANGUAGE),
                            @ExampleObject(name = "INVALID_RECOVERY_INFO", value = INVALID_RECOVERY_INFO_JSON),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                            @ExampleObject(name = "DUPLICATE_DELIVERY_METHOD", value = DUPLICATE_DELIVERY_METHOD),
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody RecoveryCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for recovery certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr());
        createDto.validate();

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logKpi(KPI_TYPE_RECOVERY, userExtId, createDto, covidCertificate.getUvci(), null, createDto.getRecoveryInfo().get(0).getCountryOfTest());
        return covidCertificate;
    }

    @PostMapping("/antibody")
    @Operation(operationId = "createAntibodyCertificate",
            summary = "Creates an antibody certificate for the given data.",
            description = "Creates an antibody certificate as a QR-Code and PDF. Performs an integrity check for each request based on headers and body.",
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
                            @ExampleObject(name = "INVALID_DATE_OF_BIRTH_IN_FUTURE", value = INVALID_DATE_OF_BIRTH_IN_FUTURE),
                            @ExampleObject(name = "INVALID_GIVEN_NAME", value = INVALID_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_GIVEN_NAME", value = INVALID_STANDARDISED_GIVEN_NAME),
                            @ExampleObject(name = "INVALID_FAMILY_NAME", value = INVALID_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_STANDARDISED_FAMILY_NAME", value = INVALID_STANDARDISED_FAMILY_NAME),
                            @ExampleObject(name = "INVALID_SAMPLE_OR_RESULT_DATE_TIME", value = INVALID_SAMPLE_OR_RESULT_DATE_TIME),
                            @ExampleObject(name = "INVALID_LANGUAGE", value = INVALID_LANGUAGE),
                            @ExampleObject(name = "INVALID_ANTIBODY_INFO", value = INVALID_ANTIBODY_INFO_JSON),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                            @ExampleObject(name = "DUPLICATE_DELIVERY_METHOD", value = DUPLICATE_DELIVERY_METHOD),
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE),
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody AntibodyCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for recovery certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr());
        createDto.validate();

        CovidCertificateCreateResponseDto covidCertificate = generationService.createCovidCertificate(createDto);
        logKpi(KPI_TYPE_ANTIBODY, userExtId, createDto, covidCertificate.getUvci(), null, ISO_3166_1_ALPHA_2_CODE_SWITZERLAND);
        return covidCertificate;
    }


    public void logTestCertificateGenerationKpi(TestCertificateCreateDto createDto, String userExtId, String uvci) {
        var typeCode = Arrays.stream(TestType.values())
                .filter(testType -> Objects.equals(testType.typeCode, createDto.getTestInfo().get(0).getTypeCode()))
                .findFirst();
        String typeCodeDetailString = null;
        if (typeCode.isPresent() && typeCode.get().equals(TestType.PCR)) {
            typeCodeDetailString = "pcr";
        } else if (typeCode.isPresent() && typeCode.get().equals(TestType.RAPID_TEST)) {
            typeCodeDetailString = "rapid";
        }
        logKpi(KPI_TYPE_TEST, userExtId, createDto, uvci, typeCodeDetailString, createDto.getTestInfo().get(0).getMemberStateOfTest());
    }

    private void logKpi(String type, String userExtId, CertificateCreateDto createDto, String uvci, String details, String country) {
        LocalDateTime timestamp = LocalDateTime.now();
        kpiDataService.saveKpiData(timestamp, type, userExtId, uvci, details, country);
        var timestampKVPair = kv(KPI_TIMESTAMP_KEY, timestamp.format(LOG_FORMAT));
        var systemKVPair = kv(KPI_CREATE_CERTIFICATE_TYPE, KPI_SYSTEM_API);
        var typeKVPair = kv(KPI_TYPE_KEY, type);
        var detailsKVPair = kv(KPI_DETAILS_KEY, details);
        var kpiCountryKVPair = kv(KPI_COUNTRY, country);
        var uuidKVPair = kv(KPI_UUID_KEY, userExtId);

        if (createDto.getAddress() != null && createDto.getAddress().getCantonCodeSender() != null) {
            var cantonKVPair = kv(KPI_CANTON, createDto.getAddress().getCantonCodeSender());
            if(details == null){
                log.info("kpi: {} {} {} {} {} {}", timestampKVPair, systemKVPair, typeKVPair, uuidKVPair, cantonKVPair, kpiCountryKVPair);
            }else{
                log.info("kpi: {} {} {} {} {} {} {}", timestampKVPair, systemKVPair, typeKVPair,  detailsKVPair, uuidKVPair, cantonKVPair, kpiCountryKVPair);
            }
            kpiDataService.saveKpiData(timestamp, KPI_CANTON, createDto.getAddress().getCantonCodeSender(), uvci, details, country);
        } else if (StringUtils.hasText(createDto.getAppCode())) {
            var inAppDeliveryTypeKVPair = kv(KPI_TYPE_KEY, KPI_TYPE_INAPP_DELIVERY);
            if(details == null){
                log.info("kpi: {} {} {} {} {}", timestampKVPair, systemKVPair, inAppDeliveryTypeKVPair, uuidKVPair, kpiCountryKVPair);
            }else{
                log.info("kpi: {} {} {} {} {} {}", timestampKVPair, systemKVPair, inAppDeliveryTypeKVPair, detailsKVPair, uuidKVPair, kpiCountryKVPair);
            }
            kpiDataService.saveKpiData(timestamp, KPI_TYPE_INAPP_DELIVERY, userExtId, uvci, details, country);
        } else {
            if(details == null){
                log.info("kpi: {} {} {} {} {}", timestampKVPair, systemKVPair, typeKVPair, uuidKVPair, kpiCountryKVPair);
            }else{
                log.info("kpi: {} {} {} {} {} {}", timestampKVPair, systemKVPair, typeKVPair, detailsKVPair, uuidKVPair, kpiCountryKVPair);
            }
        }
    }
}
