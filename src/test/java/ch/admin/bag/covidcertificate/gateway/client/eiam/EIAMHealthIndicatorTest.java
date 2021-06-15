package ch.admin.bag.covidcertificate.gateway.client.eiam;

import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Client;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryClientsResponse;
import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EIAMHealthIndicatorTest {
    private final JFixture jFixture = new JFixture();

    @Mock
    public EIAMClient eiamClient;
    @InjectMocks
    private EIAMHealthIndicator healthIndicator;

    @Test
    void givenClientExists_whenHealth_thenReturnsUP() {
        // given
        when(eiamClient.queryClient(any(String.class)))
                .thenReturn(getQueryClientsResponse("GGG"));
        // when
        Health result = healthIndicator.health();
        // then
        assertEquals(Status.UP, result.getStatus());
    }

    @Test
    void givenClientNotExists_whenHealth_thenReturnsDOWN() {
        // given
        when(eiamClient.queryClient(any(String.class)))
                .thenReturn(new QueryClientsResponse());
        // when
        Health result = healthIndicator.health();
        // then
        assertEquals(Status.DOWN, result.getStatus());
    }

    @Test
    void givenWrongClientReturned_whenHealth_thenReturnsDOWN() {
        // given
        when(eiamClient.queryClient(any(String.class)))
                .thenReturn(getQueryClientsResponse(jFixture.create(String.class)));
        // when
        Health result = healthIndicator.health();
        // then
        assertEquals(Status.DOWN, result.getStatus());
    }

    @Test
    void givenExceptionIsThrown_whenHealth_thenReturnsDOWN() {
        // given
        when(eiamClient.queryClient(any(String.class)))
                .thenThrow(RuntimeException.class);
        // when
        Health result = healthIndicator.health();
        // then
        assertEquals(Status.DOWN, result.getStatus());
    }

    private QueryClientsResponse getQueryClientsResponse(String clientName) {
        var queryClientsResponse = new QueryClientsResponse();
        var clients = queryClientsResponse.getReturns();
        var client = new Client();
        client.setName(clientName);
        clients.add(client);
        return queryClientsResponse;
    }
}
