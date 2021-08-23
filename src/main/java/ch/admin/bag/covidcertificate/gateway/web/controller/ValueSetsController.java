package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import ch.admin.bag.covidcertificate.gateway.service.AuthorizationService;
import ch.admin.bag.covidcertificate.gateway.service.InvalidBearerTokenException;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.*;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/valuesets")
@RequiredArgsConstructor
public class ValueSetsController {

    private final AuthorizationService authorizationService;

    @PostMapping("/rapid-tests")
    @Operation(operationId = "getRapidTests",
            summary = "Gets a list of all rapid tests.",
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
        var rapidTests = new ArrayList<RapidTestDto>();
        rapidTests.add(new RapidTestDto("1341", "Qingdao Hightop Biotech Co., Ltd, SARS-CoV-2 Antigen Rapid Test (Immunochromatography)", true));
        rapidTests.add(new RapidTestDto("1065", "Becton Dickinson, BD Veritor? System for Rapid Detection of SARS CoV 2", true));
        return rapidTests;
    }

    @PostMapping("/issuable-rapid-tests")
    @Operation(operationId = "getIssuableRapidTests",
            summary = "Gets a list of all issuable rapid tests.",
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
        var issuableRapidTests = new ArrayList<IssuableRapidTestDto>();
        issuableRapidTests.add(new IssuableRapidTestDto("1341", "Qingdao Hightop Biotech Co., Ltd, SARS-CoV-2 Antigen Rapid Test (Immunochromatography)"));
        issuableRapidTests.add(new IssuableRapidTestDto("1065", "Becton Dickinson, BD Veritor? System for Rapid Detection of SARS CoV 2"));
        return issuableRapidTests;
    }

    @PostMapping("/vaccines")
    @Operation(operationId = "getVaccines",
            summary = "Gets a list of all vaccines.",
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
        var vaccines = new ArrayList<VaccineDto>();
        vaccines.add(new VaccineDto("EU/1/20/1528", "Comirnaty", "1119349007", "SARS-CoV-2 mRNA vaccine", "ORG-100030215", "Biontech Manufacturing GmbH", true));
        vaccines.add(new VaccineDto("EU/1/20/1507", "COVID-19 Vaccine Moderna", "1119349007", "SARS-CoV-2 mRNA vaccine", "ORG-100031184", "Moderna Biotech Spain S.L.", true));
        return vaccines;
    }

    @PostMapping("/issuable-vaccines")
    @Operation(operationId = "getIssuableVaccines",
            summary = "Gets a list of all issuable vaccines.",
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
        var issuableVaccines = new ArrayList<IssuableVaccineDto>();
        issuableVaccines.add(new IssuableVaccineDto("EU/1/20/1528", "Comirnaty", "1119349007", "SARS-CoV-2 mRNA vaccine", "ORG-100030215", "Biontech Manufacturing GmbH"));
        issuableVaccines.add(new IssuableVaccineDto("EU/1/20/1507", "COVID-19 Vaccine Moderna", "1119349007", "SARS-CoV-2 mRNA vaccine", "ORG-100031184", "Moderna Biotech Spain S.L."));
        return issuableVaccines;
    }
}
