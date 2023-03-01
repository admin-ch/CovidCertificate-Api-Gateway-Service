package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.features.authorization.Function;
import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateRevocationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.dto.RevocationListResponseDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationListDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/covidcertificate/revoke")
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
public class CovidCertificateRevocationController {

    private final CovidCertificateRevocationService revocationService;
    private final AuthorizationService authorizationService;


    @PostMapping
    @Operation(operationId = "revokeCertificate",
            summary = "Revokes the certificate for the given UVCI.",
            description = "Revokes a Covid certificate with a given UVCI. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "201", description = "CREATED", content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "400",
            content = @Content(
                    schema = @Schema(implementation = RestError.class),
                    mediaType = "application/json",
                    examples = {@ExampleObject(name = "INVALID_UVCI", value = INVALID_UVCI)}
            )
    )
    @ApiResponse(responseCode = "409",
            content = @Content(
                    schema = @Schema(implementation = RestError.class),
                    mediaType = "application/json",
                    examples = {@ExampleObject(name = "DUPLICATE_UVCI", value = DUPLICATE_UVCI)}
            )
    )
    public ResponseEntity<HttpStatus> create(@RequestBody RevocationDto revocationDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Revoke for covid certificate");
        String userExtId = authorizationService.validateAndGetId(revocationDto, request.getRemoteAddr(), Function.REVOKE_CERTIFICATE);

        revocationService.createRevocation(revocationDto, userExtId);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/mass-revocation")
    @Operation(operationId = "certificateMassRevocationCheck",
            summary = "Executes a mass-revocation of the given UVCIs.",
            description = "Revokes all revokable UVCIs of list of UVCIs. Performs checks if the UVCI is well formatted, known and not yet revoked.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RevocationListResponseDto.class)))
    @ApiResponse(responseCode = "400",
            content = @Content(
                    schema = @Schema(implementation = RestError.class),
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "INVALID_SIZE_OF_UVCI_LIST", value = INVALID_SIZE_OF_UVCI_LIST)
                    }
            )
    )
    public RevocationListResponseDto createMassRevocation(@RequestBody RevocationListDto revocationListDto, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of Mass-Revocation for covid certificate");
        String userExtId = authorizationService.validateAndGetId(revocationListDto, request.getRemoteAddr(), Function.BULK_REVOKE_CERTIFICATES);

        return revocationService.createMassRevocation(revocationListDto, userExtId);
    }

}
