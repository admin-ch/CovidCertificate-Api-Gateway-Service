package ch.admin.bag.covidcertificate.gateway.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRevocationRepository extends JpaRepository<OtpRevocation, String> {
}
