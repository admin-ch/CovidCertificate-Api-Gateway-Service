package ch.admin.bag.covidcertificate.gateway.client.eiam;

import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.Client;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.ClientQuery;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.DetailLevel;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.DetailLevels;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryClients;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryClientsResponse;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsers;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.QueryUsersResponse;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.SamlFederation;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.User;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.UserQuery;
import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.UserState;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.text.MessageFormat;

public class EIAMClient extends WebServiceGatewaySupport {

    private static final String PREFIX_EID_5300 = "eid\\5300\\";
    private static final String QUERYTYPE_CANT_BE_NULL = "Query type can't be null!";
    private static final String QUERYTYPE_NOT_SUPPORTED_ERROR_MESSAGE = "QueryType {1} is not (yet) supported!";

    public QueryUsersResponse requestUsers(String uuid, String idpSource, QueryType queryType) {
        UserQuery userQuery = createUserQuery(uuid, idpSource, queryType);
        var requestPayload = new QueryUsers();
        requestPayload.setQuery(userQuery);
        return (QueryUsersResponse) getWebServiceTemplate()
                .marshalSendAndReceive(requestPayload);
    }

    public QueryClientsResponse queryClient(String clientName) {
        var client = new Client();
        client.setName(clientName);
        var clientQuery = new ClientQuery();
        clientQuery.setClient(client);
        var request = new QueryClients();
        request.setQuery(clientQuery);
        return (QueryClientsResponse) getWebServiceTemplate()
                .marshalSendAndReceive(request);
    }

    private UserQuery createUserQuery(String uuid, String idpSource, QueryType queryType) {
        User user = createUser(uuid, idpSource, queryType);
        DetailLevels detailLevels = createDetailLevels();

        var userQuery = new UserQuery();
        userQuery.setClientName(EIAMConfig.CLIENT_NAME);
        userQuery.setDetailLevels(detailLevels);
        userQuery.setUser(user);
        return userQuery;
    }

    private User createUser(String uuid, String idpSource, QueryType queryType) {

        if (queryType == null) throw new NullPointerException(QUERYTYPE_CANT_BE_NULL);

        User user;

        switch (queryType) {
            case BY_USER_EXT_ID:
                user = createUserWithExtId(uuid);
                break;
            case BY_USER_CH_LOGIN_SUBJECT:
                user = createUserWithCHLogin(uuid);
                break;
            case BY_USER_HIN_LOGIN_SUBJECT:
                user = createUserWithHINLogin(uuid);
                break;
            case BY_SUBJECT_AND_ISSUER:
                user = createUserWithSubjectAndIssuer(uuid, idpSource);
                break;
            default:
                throw new UnsupportedOperationException(MessageFormat.format(QUERYTYPE_NOT_SUPPORTED_ERROR_MESSAGE, queryType.name()));
        }

        user.setState(UserState.ACTIVE);

        return user;
    }

    private User createUserWithExtId(String userExtId) {
        var user = new User();
        user.setExtId(userExtId);
        return user;
    }

    private User createUserWithCHLogin(String uuid) {
        var user = new User();
        user.getSamlFederations().add(createSamlFederation(PREFIX_EID_5300 + uuid));
        return user;
    }

    private User createUserWithHINLogin(String subjectNameId) {
        var user = new User();
        user.getSamlFederations().add(createSamlFederation(subjectNameId));
        return user;
    }

    private User createUserWithSubjectAndIssuer(String uuid, String idpsource) {
        var user = new User();

        var samlFederation = new SamlFederation();
        samlFederation.setSubjectNameId(uuid);
        samlFederation.setIssuerNameId(idpsource);

        user.getSamlFederations().add(samlFederation);
        return user;
    }

    private SamlFederation createSamlFederation(String subjectNameId) {
        var samlFederation = new SamlFederation();
        samlFederation.setSubjectNameId(subjectNameId);
        return samlFederation;
    }

    private DetailLevels createDetailLevels() {
        var detailLevels = new DetailLevels();
        detailLevels.setUserDetailLevel(DetailLevel.HIGH);
        detailLevels.setProfileDetailLevel(DetailLevel.HIGH);
        detailLevels.setAuthorizationDetailLevel(DetailLevel.HIGH);
        detailLevels.setDefaultDetailLevel(DetailLevel.HIGH);
        return detailLevels;
    }
}
