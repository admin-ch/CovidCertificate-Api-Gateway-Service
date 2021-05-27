package ch.admin.bag.covidcertificate.gateway.service.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class AuthorizationCodeCreateDto {

    private String comment;

    private String name;

}
