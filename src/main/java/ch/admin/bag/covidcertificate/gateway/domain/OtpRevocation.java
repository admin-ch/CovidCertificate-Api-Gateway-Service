package ch.admin.bag.covidcertificate.gateway.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "otprevocation")
public class OtpRevocation {
    @Id
    String jti;
    String userExtId;
    String idpsource;
    LocalDateTime createdAt;
}
