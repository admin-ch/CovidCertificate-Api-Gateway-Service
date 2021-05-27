package ch.admin.bag.covidcertificate.gateway.service.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class AuthorizationCodeResponseDto {

    private String authorizationCode;

}
