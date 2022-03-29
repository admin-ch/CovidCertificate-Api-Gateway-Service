package ch.admin.bag.covidcertificate.gateway.client.internal;

import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMClient;
import ch.admin.bag.covidcertificate.gateway.client.eiam.EIAMConfig;
import ch.admin.bag.covidcertificate.gateway.client.eiam.QueryType;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.web.config.ProfileRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
@org.springframework.context.annotation.Profile("!" + ProfileRegistry.IDENTITY_AUTHORIZATION_MOCK)
@RequiredArgsConstructor
public class DefaultIdentityAuthorizationClient extends AbstractIdentityAuthorizationClient {

    private static final String LOG_KEY_UUID = "uuid";
    private static final String LOG_KEY_IDPSOURCE = "idpSource";
    private static final String LOG_KEY_QUERY_TYPE = "queryType";
    private static final String LOG_KEY_CLIENTNAME = "clientName";

    private final EIAMClient eiamClient;

    protected User searchUser(String uuid, String idpSource) {
        log.info("Search user with {} {} {}",
                kv(LOG_KEY_UUID, uuid),
                kv(LOG_KEY_IDPSOURCE, idpSource),
                kv(LOG_KEY_CLIENTNAME, EIAMConfig.CLIENT_NAME));

        QueryType queryType = QueryType.BY_USER_EXT_ID;

        // First, the user is searched considering UUID as userExtID which is the main case...
        log.info("Calling eIAM by userExtId");
        List<User> eiamUsers = requestUsers(uuid, idpSource, queryType);

        if (CollectionUtils.isEmpty(eiamUsers) && QueryType.BY_USER_CH_LOGIN_SUBJECT.getIdpSource().equals(idpSource)) {
            // ...if the previous search does not return a response
            // a new search is performed considering the given credential as of CH-LOGIN type
            // since it's possible to provide a CH-Login credential (idpSource:E-ID CH-LOGIN)...
            log.info("...no result returned, calling eIAM by CH-LOGIN");
            queryType = QueryType.BY_USER_CH_LOGIN_SUBJECT;
            eiamUsers = requestUsers(uuid, idpSource, queryType);
        } else if (CollectionUtils.isEmpty(eiamUsers) && QueryType.BY_USER_HIN_LOGIN_SUBJECT.getIdpSource().equals(idpSource)) {
            // ...if the previous search does also not return a response
            // a new search is performed considering the given credential as of HIN-LOGIN type
            // since it's possible to provide a HIN-LOGIN credential (idpSource:HIN).
            log.info("...no result returned, calling eIAM by HIN-LOGIN");
            queryType = QueryType.BY_USER_HIN_LOGIN_SUBJECT;
            eiamUsers = requestUsers(uuid, idpSource, queryType);
        }

        if (CollectionUtils.isEmpty(eiamUsers)) {
            // ...if the previous search does also not return a response
            // a new search is performed considering the given credential as of legacy SubjectAndIssuer type
            // since it's possible to provide a legacy classical SubjectAndIssuer credential (case: NAS).
            log.info("...no result returned, calling eIAM by SUBJECT&ISSUER");
            queryType = QueryType.BY_SUBJECT_AND_ISSUER;
            eiamUsers = requestUsers(uuid, idpSource, queryType);
        }

        Optional<User> optionalUser = eiamUsers.stream().findFirst();

        if (optionalUser.isEmpty()) {
            log.info("User could not been found in eIAM. {} {} {}",
                    kv(LOG_KEY_UUID, uuid),
                    kv(LOG_KEY_IDPSOURCE, idpSource),
                    kv(LOG_KEY_CLIENTNAME, EIAMConfig.CLIENT_NAME));

            throw new IllegalArgumentException("User does not exist in eIAM.");
        }

        log.info("User has been found in eIAM. {} {} {} {}",
                kv(LOG_KEY_UUID, uuid),
                kv(LOG_KEY_IDPSOURCE, idpSource),
                kv(LOG_KEY_QUERY_TYPE, queryType),
                kv(LOG_KEY_CLIENTNAME, EIAMConfig.CLIENT_NAME));

        return optionalUser.get();
    }

    private List<User> requestUsers(String uuid, String idpSource, QueryType type) {
        return eiamClient.requestUsers(uuid, idpSource, type).getReturns();
    }
}
