package ch.admin.bag.covidcertificate.gateway.client.eiam;

import ch.admin.bag.covidcertificate.gateway.eiam.adminservice.*;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.util.List;

public class EIAMClient extends WebServiceGatewaySupport {
    private static final String CLIENT_NAME = "GGG";

    public QueryUsersResponse queryUser(String uuid, String idpSource) {
        QueryUsers request = getQueryUsers(uuid, idpSource);
        return (QueryUsersResponse) getWebServiceTemplate().marshalSendAndReceive(request);
    }

    private QueryUsers getQueryUsers(String uuid, String idpSource) {
        QueryUsers request = new QueryUsers();
        request.setQuery(getUserQuery(uuid, idpSource));
        return request;
    }

    private UserQuery getUserQuery(String uuid, String idpSource) {
        UserQuery userQuery = new UserQuery();
        userQuery.setClientName(CLIENT_NAME);
        userQuery.setDetailLevels(getDetailLevels());
        userQuery.setUser(getUser(uuid, idpSource));
        return userQuery;
    }

    private User getUser(String uuid, String idpSource) {
        User user = new User();
        user.setState(UserState.ACTIVE);
        List<SamlFederation> samlFederations = user.getSamlFederations();
        samlFederations.add(getSamlFederation(uuid, idpSource));
        return user;
    }

    private SamlFederation getSamlFederation(String uuid, String idpSource) {
        SamlFederation samlFederation = new SamlFederation();
        samlFederation.setSubjectNameId(uuid);
        samlFederation.setIssuerNameId(idpSource);
        return samlFederation;
    }

    private DetailLevels getDetailLevels() {
        DetailLevels detailLevels = new DetailLevels();
        detailLevels.setUserDetailLevel(DetailLevel.MEDIUM);
        detailLevels.setProfileDetailLevel(DetailLevel.MEDIUM);
        detailLevels.setAuthorizationDetailLevel(DetailLevel.LOW);
        detailLevels.setDefaultDetailLevel(DetailLevel.EXCLUDE);
        return detailLevels;
    }
}
