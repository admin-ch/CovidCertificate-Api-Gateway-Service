package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.ValueSetsService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableRapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableVaccineDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccineDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/valuesets")
@RequiredArgsConstructor
public class ValueSetsController {

    private final ValueSetsService valueSetsService;

    @GetMapping("/rapid-tests")
    @Operation(operationId = "rapidTests",
               summary = "Gets a list of all rapid tests. *Only available for testing on ABN.",
               description = "Gets a list of all rapid tests based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
               parameters = {
                       @Parameter(in = ParameterIn.HEADER,
                                  name = IntegrityFilter.HEADER_HASH_NAME,
                                  required = true,
                                  description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                                          "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                                          "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                                  schema = @Schema(type = "string", format = "Base64")
                       )
               }
    )
    @ApiResponse(responseCode = "200",
                 content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = RapidTestDto.class))))
    public List<RapidTestDto> rapidTests() {
        log.info("Call of rapidTests for value sets");
        return valueSetsService.getRapidTests();
    }

    @GetMapping("/issuable-rapid-tests")
    @Operation(operationId = "issuableRapidTests",
               summary = "Gets a list of all issuable rapid tests. *Only available for testing on ABN.",
               description = "Gets a list of all issuable rapid tests accepted by the BAG based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
               parameters = {
                       @Parameter(in = ParameterIn.HEADER,
                                  name = IntegrityFilter.HEADER_HASH_NAME,
                                  required = true,
                                  description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                                          "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                                          "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                                  schema = @Schema(type = "string", format = "Base64")
                       )
               }
    )
    @ApiResponse(responseCode = "200",
                 content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IssuableRapidTestDto.class))))
    public List<IssuableRapidTestDto> issuableRapidTests() {
        log.info("Call of issuableRapidTests for value sets");
        return valueSetsService.getIssuableRapidTests();
    }

    @GetMapping("/vaccines")
    @Operation(operationId = "vaccines",
               summary = "Gets a list of all vaccines. *Only available for testing on ABN.",
               description = "Gets a list of all vaccines based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
               parameters = {
                       @Parameter(in = ParameterIn.HEADER,
                                  name = IntegrityFilter.HEADER_HASH_NAME,
                                  required = true,
                                  description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                                          "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                                          "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                                  schema = @Schema(type = "string", format = "Base64")
                       )
               }
    )
    @ApiResponse(responseCode = "200",
                 content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = VaccineDto.class))))
    public List<VaccineDto> vaccines() {
        log.info("Call of vaccines for value sets");
        return valueSetsService.getVaccines();
    }

    @GetMapping("/issuable-vaccines")
    @Operation(operationId = "issuableVaccines",
               summary = "Gets a list of all issuable vaccines. *Only available for testing on ABN.",
               description = "Gets a list of all issuable vaccines accepted by the BAG based on the official list of the EU. Performs an integrity check for each request based on headers and body.",
               parameters = {
                       @Parameter(in = ParameterIn.HEADER,
                                  name = IntegrityFilter.HEADER_HASH_NAME,
                                  required = true,
                                  description = "Base64 encoded hash of the canonicalized body, generated with the `SHA256withRSA` algorithm " +
                                          "signed with the private key of the certificate issued by \"SwissGov Regular CA 01\". " +
                                          "See [documentation](https://github.com/admin-ch/CovidCertificate-Apidoc#content-signature) on Github.",
                                  schema = @Schema(type = "string", format = "Base64")
                       )
               }
    )
    @ApiResponse(responseCode = "200",
                 content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = IssuableVaccineDto.class))))
    public List<IssuableVaccineDto> issuableVaccines() {
        log.info("Call of issuableVaccines for value sets");
        return valueSetsService.getIssuableVaccines();
    }
}
