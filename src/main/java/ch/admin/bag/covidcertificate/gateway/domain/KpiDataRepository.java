package ch.admin.bag.covidcertificate.gateway.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KpiDataRepository extends JpaRepository<KpiData, UUID> {
}
