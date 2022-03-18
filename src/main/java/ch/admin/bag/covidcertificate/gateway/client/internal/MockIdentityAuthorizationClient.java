package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.service.dto.CreateCertificateException;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.EIAM_CALL_ERROR;
import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.INVALID_IDENTITY_USER;

@Service
@Slf4j
@Profile(ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
public class MockIdentityAuthorizationClient extends AbstractIdentityAuthorizationClient {
    public static final String TEST_PROFILE_AUTHORIZED_FIRST_ACTIVE_ROLE_XML = "test_profile_authorized_first_active_role.xml";

    protected User queryUser(String uuid, String idpSource) {
        String fileName = TEST_PROFILE_AUTHORIZED_FIRST_ACTIVE_ROLE_XML;
        try (InputStream inputStream = new ClassPathResource(fileName).getInputStream()) {
            JAXBContext context = JAXBContext.newInstance(QueryUsersResponse.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            QueryUsersResponse response = (QueryUsersResponse) unmarshaller.unmarshal(inputStream);

            Optional<User> eiamUser = response.getReturns()
                    .stream().findFirst();

            if (eiamUser.isEmpty()) {
                log.info("User does not exist in file {}.", fileName);
                throw new CreateCertificateException(INVALID_IDENTITY_USER);
            }

            log.info("User has been found in file {}.", fileName);
            return eiamUser.get();
        } catch (JAXBException | IOException e) {
            log.error("Error reading file {}", fileName, e);
            throw new CreateCertificateException(EIAM_CALL_ERROR);
        }

    }
}
