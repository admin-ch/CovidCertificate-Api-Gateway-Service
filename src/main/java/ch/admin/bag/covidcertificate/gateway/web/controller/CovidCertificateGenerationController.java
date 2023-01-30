package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.features.authorization.Function;
import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateGenerationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.AntibodyCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CovidCertificateCreateResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RecoveryRatCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.TestCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationCertificateCreateDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccinationTouristCertificateCreateDto;
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

import javax.servlet.http.HttpServletRequest;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.DUPLICATE_DELIVERY_METHOD;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_ADDRESS;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_ANTIBODY_INFO_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_APP_CODE;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_BEARER_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_COUNTRY_OF_TEST;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_COUNTRY_OF_VACCINATION;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_DATE_OF_BIRTH;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_DATE_OF_BIRTH_IN_FUTURE;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_DOSES;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_FAMILY_NAME;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_GIVEN_NAME;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER_ROLE_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_LANGUAGE;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_MEDICINAL_PRODUCT;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_MEMBER_STATE_OF_TEST;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_OTP_LENGTH_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_PRINT_FOR_TEST;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_RECOVERY_INFO_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_SAMPLE_OR_RESULT_DATE_TIME;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_SIGNATURE_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_STANDARDISED_FAMILY_NAME;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_STANDARDISED_GIVEN_NAME;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_TEST_CENTER;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_TEST_INFO_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_TYP_OF_TEST;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_VACCINATION_DATE;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_VACCINATION_INFO_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.MISSING_BEARER_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.MISSING_RECOVERY_RAT_INFO_JSON;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.NO_PERSON_DATA;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.SIGNATURE_PARSE_JSON;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/covidcertificate")
@RequiredArgsConstructor
@ApiResponse(
        responseCode = "403",
        description = "All certificate creation endpoints can be toggled, when an endpoint is disabled, the backend respond with a HTTP 403 Forbidden",
        content = @Content(
                schema = @Schema(implementation = RestError.class),
                mediaType = "application/json",
                examples = {
                        @ExampleObject(name = "INVALID_BEARER", value = INVALID_BEARER_JSON),
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
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr(), Function.CREATE_VACCINE_CERTIFICATE);
        createDto.validate();

        return generationService.createCovidCertificate(createDto, userExtId);
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
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr(), Function.CREATE_VACCINE_TOURIST_CERTIFICATE);
        createDto.validate();

        return generationService.createCovidCertificate(createDto, userExtId);
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
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr(), Function.CREATE_TEST_CERTIFICATE);
        createDto.validate();

        return generationService.createCovidCertificate(createDto, userExtId);
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
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE)
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody RecoveryCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for recovery certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr(), Function.CREATE_RECOVERY_CERTIFICATE);
        createDto.validate();

        return generationService.createCovidCertificate(createDto, userExtId);
    }

    @PostMapping("/recovery-rat")
    @Operation(operationId = "createRecoveryRatCertificate",
            summary = "Creates a Rapid-Antigen-Test (RAT) based recovery certificate for the given data.",
            description = "Creates a Rapid-Antigen-Test (RAT) based recovery certificate for the given data in form of a Pdf document and a QR-Code image.",
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
                            @ExampleObject(name = "INVALID_LANGUAGE", value = INVALID_LANGUAGE),
                            @ExampleObject(name = "MISSING_RECOVERY_RAT_INFO", value = MISSING_RECOVERY_RAT_INFO_JSON),
                            @ExampleObject(name = "INVALID_ADDRESS", value = INVALID_ADDRESS),
                            @ExampleObject(name = "DUPLICATE_DELIVERY_METHOD", value = DUPLICATE_DELIVERY_METHOD),
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE)
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody RecoveryRatCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for recovery-rat certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr(), Function.CREATE_RECOVERY_RAT_CERTIFICATE);
        createDto.validate();

        return generationService.createCovidCertificate(createDto, userExtId);
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
                            @ExampleObject(name = "INVALID_APP_CODE", value = INVALID_APP_CODE)
                    }
            )
    )
    public CovidCertificateCreateResponseDto create(@RequestBody AntibodyCertificateCreateDto createDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Create for recovery certificate");
        String userExtId = authorizationService.validateAndGetId(createDto, request.getRemoteAddr(), Function.CREATE_ANTIBODY_CERTIFICATE);
        createDto.validate();

        return generationService.createCovidCertificate(createDto, userExtId);
    }
}
