package ch.admin.bag.covidcertificate.gateway.client.eiam;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.url=jdbc:h2:~/test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=sa",
        "spring.flyway.clean-on-validation-error=true"
})
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Disabled("Only runs manually")
class EIAMHealthIndicatorITTest {

    @Autowired
    private EIAMHealthIndicator healthIndicator;

    @Test
    void whenHealth_thenStatusUP() {
        // when
        Health result = healthIndicator.health();

        //then
        assertEquals(Status.UP, result.getStatus());
    }
}
