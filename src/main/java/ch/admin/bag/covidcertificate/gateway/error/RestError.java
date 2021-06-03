package ch.admin.bag.covidcertificate.gateway.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class RestError implements Serializable {
    private int errorCode;
    private String errorMessage;
    @JsonIgnore
    private HttpStatus httpStatus;

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
