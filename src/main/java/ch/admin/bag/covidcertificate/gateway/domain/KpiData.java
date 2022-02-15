package ch.admin.bag.covidcertificate.gateway.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "kpi")
public class KpiData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    LocalDateTime timestamp;
    String type;
    String value;
    String uvci;
    String details;
    String country;
    @Column(name = "in_app_delivery_code")
    String inAppDeliveryCode;

    public KpiData(LocalDateTime timestamp, String type, String value, String uvci, String details, String country) {
        this.timestamp = timestamp;
        this.type = type;
        this.value = value;
        this.uvci = uvci;
        this.details = details;
        this.country = country;
    }

    public KpiData(LocalDateTime timestamp, String type, String value, String uvci,
            String details, String country, String inAppDeliveryCode) {
        this.timestamp = timestamp;
        this.type = type;
        this.value = value;
        this.uvci = uvci;
        this.details = details;
        this.country = country;
        this.inAppDeliveryCode = inAppDeliveryCode;
    }
}
