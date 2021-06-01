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
    LocalDateTime kpiTimestamp;
    String type;
    String value;

    public KpiData(LocalDateTime kpiTimestamp, String type, String value) {
        this.kpiTimestamp = kpiTimestamp;
        this.type = type;
        this.value = value;
    }
}
