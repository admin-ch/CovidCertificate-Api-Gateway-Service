package ch.admin.bag.covidcertificate.gateway.features.authorization.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class FunctionsDefinitionDto {

    private List<Function> functions = Collections.emptyList();

    @Data
    public static class Function {
        private String identifier;
        private LocalDateTime from;
        private LocalDateTime until;
        private String mandatory;
        private List<String> oneOf;
    }
}
