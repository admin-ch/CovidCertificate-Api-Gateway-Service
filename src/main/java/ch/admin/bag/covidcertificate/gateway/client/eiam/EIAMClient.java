package ch.admin.bag.covidcertificate.gateway.client.eiam;

import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.util.List;

public class EIAMClient extends WebServiceGatewaySupport {

    public QueryUsersResponse queryUser(String uuid, String idpSource, String clientName) {
        return (QueryUsersResponse) getWebServiceTemplate()
                .marshalSendAndReceive(getQueryUsers(uuid, idpSource, clientName));
    }

    public QueryClientsResponse queryClient(String clientName) {
        return (QueryClientsResponse) getWebServiceTemplate()
                .marshalSendAndReceive(getQueryClients(clientName));
    }

    private QueryUsers getQueryUsers(String uuid, String idpSource, String clientName) {
        var request = new QueryUsers();
        request.setQuery(getUserQuery(uuid, idpSource, clientName));
        return request;
    }

    private UserQuery getUserQuery(String uuid, String idpSource, String clientName) {
        var userQuery = new UserQuery();
        userQuery.setClientName(clientName);
        userQuery.setDetailLevels(getDetailLevels());
        userQuery.setUser(getUser(uuid, idpSource));
        return userQuery;
    }

    private User getUser(String uuid, String idpSource) {
        var user = new User();
        user.setState(UserState.ACTIVE);
        List<SamlFederation> samlFederations = user.getSamlFederations();
        samlFederations.add(getSamlFederation(uuid, idpSource));
        return user;
    }

    private SamlFederation getSamlFederation(String uuid, String idpSource) {
        var samlFederation = new SamlFederation();
        samlFederation.setSubjectNameId(uuid);
        samlFederation.setIssuerNameId(idpSource);
        return samlFederation;
    }

    private DetailLevels getDetailLevels() {
        var detailLevels = new DetailLevels();
        detailLevels.setUserDetailLevel(DetailLevel.MEDIUM);
        detailLevels.setProfileDetailLevel(DetailLevel.MEDIUM);
        detailLevels.setAuthorizationDetailLevel(DetailLevel.LOW);
        detailLevels.setDefaultDetailLevel(DetailLevel.EXCLUDE);
        return detailLevels;
    }

    private QueryClients getQueryClients(String clientName) {
        var request = new QueryClients();
        request.setQuery(getClientQuery(clientName));
        return request;
    }

    private ClientQuery getClientQuery(String clientName) {
        var clientQuery = new ClientQuery();
        clientQuery.setClient(getClient(clientName));
        return clientQuery;
    }

    private Client getClient(String clientName) {
        Client client = new Client();
        client.setName(clientName);
        return client;
    }
}
