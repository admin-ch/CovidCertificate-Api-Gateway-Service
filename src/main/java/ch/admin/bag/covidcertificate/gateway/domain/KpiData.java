package ch.admin.bag.covidcertificate.gateway.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    public KpiData(LocalDateTime timestamp, String type, String value, String uvci) {
        this.timestamp = timestamp;
        this.type = type;
        this.value = value;
        this.uvci = uvci;
    }
}
