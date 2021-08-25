package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.ValueSetsService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableRapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableVaccineDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccineDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.ValueSetsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/valuesets")
@RequiredArgsConstructor
public class ValueSetsController {

    private final ValueSetsService valueSetsService;
    private final AuthorizationService authorizationService;

    @PostMapping("/rapid-tests")
    @Operation(operationId = "getRapidTests",
            summary = "Gets a list of all rapid tests. *Only available for testing on ABN.",
            description = "Gets a list of all rapid tests based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RapidTestDto.class))))
    public List<RapidTestDto> getRapidTests(@RequestBody ValueSetsDto valueSets, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of getRapidTests for value sets");
        authorizationService.validateAndGetId(valueSets, request.getRemoteAddr());
        return valueSetsService.getRapidTests();
    }

    @PostMapping("/issuable-rapid-tests")
    @Operation(operationId = "getIssuableRapidTests",
            summary = "Gets a list of all issuable rapid tests. *Only available for testing on ABN.",
            description = "Gets a list of all issuable rapid tests defined by the BAG based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IssuableRapidTestDto.class))))
    public List<IssuableRapidTestDto> getIssuableRapidTests(@RequestBody ValueSetsDto valueSets, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of getIssuableRapidTests for value sets");
        authorizationService.validateAndGetId(valueSets, request.getRemoteAddr());
        return valueSetsService.getIssuableRapidTests();
    }

    @PostMapping("/vaccines")
    @Operation(operationId = "getVaccines",
            summary = "Gets a list of all vaccines. *Only available for testing on ABN.",
            description = "Gets a list of all vaccines based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VaccineDto.class))))
    public List<VaccineDto> getVaccines(@RequestBody ValueSetsDto valueSets, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of getVaccines for value sets");
        authorizationService.validateAndGetId(valueSets, request.getRemoteAddr());
        return valueSetsService.getVaccines();
    }

    @PostMapping("/issuable-vaccines")
    @Operation(operationId = "getIssuableVaccines",
            summary = "Gets a list of all issuable vaccines. *Only available for testing on ABN.",
            description = "Gets a list of all issuable vaccines defined by the BAG based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = IntegrityFilter.HEADER_HASH_NAME,
                            required = true, description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                            "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                            "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                            schema = @Schema(type = "string", format = "Base64")
                    )
            }
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IssuableVaccineDto.class))))
    public List<IssuableVaccineDto> getIssuableVaccines(@RequestBody ValueSetsDto valueSets, HttpServletRequest request) throws InvalidBearerTokenException {
        log.info("Call of getIssuableVaccines for value sets");
        authorizationService.validateAndGetId(valueSets, request.getRemoteAddr());
        return valueSetsService.getIssuableVaccines();
    }
}
