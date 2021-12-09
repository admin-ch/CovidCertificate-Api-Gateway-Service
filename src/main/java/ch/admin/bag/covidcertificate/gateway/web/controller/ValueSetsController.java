package ch.admin.bag.covidcertificate.gateway.web.controller;

import ch.admin.bag.covidcertificate.gateway.service.ValueSetsService;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CountryCodeDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.CountryCodesDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableRapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.IssuableVaccineDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.RapidTestDto;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.SystemSource;
import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.VaccineDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            summary = "Gets a list of all rapid tests.",
            description = "Gets a list of all rapid tests based on the official list of the EU. Performs an integrity check for each request based on headers and body."
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RapidTestDto.class))))
    public List<RapidTestDto> rapidTests() {
        log.info("Call of rapidTests for value sets");
        return valueSetsService.getRapidTests();
    }

    @GetMapping("/issuable-rapid-tests")
    @Operation(operationId = "issuableRapidTests",
            summary = "Gets a list of all issuable rapid tests.",
            description = "Gets a list of all issuable rapid tests accepted by the BAG based on the official list of the EU. Performs an integrity check for each request based on headers and body."
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IssuableRapidTestDto.class))))
    public List<IssuableRapidTestDto> issuableRapidTests() {
        log.info("Call of issuableRapidTests for value sets");
        return valueSetsService.getIssuableRapidTests();
    }

    @GetMapping("/vaccines")
    @Operation(operationId = "vaccines",
            summary = "Gets a list of all vaccines.",
            description = "Gets a list of all vaccines based on the official list of the EU. Performs an integrity check for each request based on headers and body."
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VaccineDto.class))))
    public List<VaccineDto> vaccines() {
        log.info("Call of vaccines for value sets");
        return valueSetsService.getVaccines();
    }

    @GetMapping("/issuable-vaccines")
    @Operation(operationId = "issuableVaccines",
            summary = "Gets a list of all issuable vaccines.",
            description = "Gets a list of all issuable vaccines accepted by the BAG based on the official list of the EU. Performs an integrity check for each request based on headers and body."
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IssuableVaccineDto.class))))
    public List<IssuableVaccineDto> issuableVaccines() {
        log.info("Call of issuableVaccines for value sets with systemSource {}", SystemSource.ApiGateway);
        return valueSetsService.getIssuableVaccines(SystemSource.ApiGateway);
    }

    @GetMapping("/issuable-vaccines/{systemSource}")
    @Operation(operationId = "issuableVaccines",
               summary = "Gets a list of all issuable vaccines.",
               description = "Gets a list of all issuable vaccines accepted by the BAG based on the official list of the EU. Performs an integrity check for each request based on headers and body."
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IssuableVaccineDto.class))))
    public List<IssuableVaccineDto> issuableVaccines(@PathVariable String systemSource) {
        final SystemSource localSystemSource;
        if(StringUtils.hasText(systemSource)) {
            localSystemSource = SystemSource.valueOf(systemSource);
        } else {
            localSystemSource = SystemSource.ApiGateway;
        }
        log.info("Call of issuableVaccines for value sets with systemSource {}", localSystemSource);
        return valueSetsService.getIssuableVaccines(localSystemSource);
    }

    @GetMapping("/countries")
    @Operation(operationId = "countryCodes",
            summary = "Gets a list of all countryCodes for every language.",
            description = "Gets a list of all countryCodes for every supported language. Performs an integrity check for each request based on headers and body."
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CountryCodesDto.class))))
    public CountryCodesDto countryCodes() {
        log.info("Call of getCountryCodes for value sets");
        return valueSetsService.getCountryCodes();
    }

    @GetMapping("/countries/{language}")
    @Operation(operationId = "countryCodesByLanguage",
            summary = "Gets a list of all countryCodes for a specific language.",
            description = "Gets a list of all countryCodes for a specific supported language. Performs an integrity check for each request based on headers and body."
    )
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CountryCodeDto.class))))
    public List<CountryCodeDto> countryCodeByLanguage(@PathVariable final String language) {
        log.info("Call of getCountryCodesByLanguage for value sets");
        return valueSetsService.getCountryCodesByLanguage(language);
    }
}
