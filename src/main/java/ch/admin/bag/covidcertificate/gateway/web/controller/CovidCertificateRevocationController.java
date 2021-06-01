package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.error.RestError;
import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.BearerTokenValidationService;
import ch.admin.bag.covidcertificate.gateway.service.CovidCertificateRevocationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.KpiDataService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RevocationDto;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static ch.admin.bag.covidcertificate.gateway.Constants.*;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.DUPLICATE_UVCI;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_UVCI;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/covidcertificate/revoke")
@RequiredArgsConstructor
public class CovidCertificateRevocationController {

    private final CovidCertificateRevocationService service;

    private final BearerTokenValidationService bearerTokenValidationService;

    private final KpiDataService kpiDataService;

    @PostMapping
    @PreAuthorize("hasRole('bag-cc-certificatecreator')")
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
    public ResponseEntity<HttpStatus> create(@RequestBody RevocationDto revocationDto) throws InvalidBearerTokenException {
        log.info("Call of Revoke for covid certificate");
        String userExtId = bearerTokenValidationService.validate(revocationDto.getOtp());

        service.createRevocation(revocationDto);

        LocalDateTime timestamp = LocalDateTime.now();
        log.info("kpi: {} {} {}", kv(KPI_TIMESTAMP_KEY, timestamp.format(LOG_FORMAT)), kv(KPI_REVOKE_CERTIFICATE_TYPE, KPI_SYSTEM_API), kv(KPI_UUID_KEY, userExtId));
        kpiDataService.saveKpiData(timestamp, KPI_REVOKE_CERTIFICATE_TYPE, userExtId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
