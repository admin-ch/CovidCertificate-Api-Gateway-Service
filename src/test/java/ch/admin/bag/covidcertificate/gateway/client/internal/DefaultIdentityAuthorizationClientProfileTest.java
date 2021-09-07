package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultIdentityAuthorizationClientProfileTest {

    private DefaultIdentityAuthorizationClient authorizationClient;

    @BeforeEach
    void beforeEach() {
        authorizationClient = new DefaultIdentityAuthorizationClient(new EIAMClient());
    }

    @Test
    void hasUserRoleSuperUserOrCreator_not_authorized_ok() throws JAXBException, IOException {
        QueryUsersResponse response = readExampleResponse("test_profile_not_authorized.xml");
        boolean isSuperUserOrCreator = authorizationClient.hasUserRoleSuperUserOrCreator(response);
        assertThat(isSuperUserOrCreator).isFalse();
    }

    @Test
    void hasUserRoleSuperUserOrCreator_not_authorized_two_active_ok() throws JAXBException, IOException {
        QueryUsersResponse response = readExampleResponse("test_profile_not_authorized_two_active.xml");
        boolean isSuperUserOrCreator = authorizationClient.hasUserRoleSuperUserOrCreator(response);
        assertThat(isSuperUserOrCreator).isFalse();
    }

    @Test
    void hasUserRoleSuperUserOrCreator_authorized_ok() throws JAXBException, IOException {
        QueryUsersResponse response = readExampleResponse("test_profile_authorized.xml");
        boolean isSuperUserOrCreator = authorizationClient.hasUserRoleSuperUserOrCreator(response);
        assertThat(isSuperUserOrCreator).isTrue();
    }

    @Test
    void hasUserRoleSuperUserOrCreator_authorized_two_active_ok() throws JAXBException, IOException {
        QueryUsersResponse response = readExampleResponse("test_profile_authorized_two_active.xml");
        boolean isSuperUserOrCreator = authorizationClient.hasUserRoleSuperUserOrCreator(response);
        assertThat(isSuperUserOrCreator).isTrue();
    }

    private QueryUsersResponse readExampleResponse(String fileName) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(QueryUsersResponse.class);
        return (QueryUsersResponse) context.createUnmarshaller().unmarshal(this.getXMLResource(fileName));
    }

    private InputStream getXMLResource(String fileName) throws IOException {
        return new ClassPathResource(fileName, this.getClass()).getInputStream();
    }
}
