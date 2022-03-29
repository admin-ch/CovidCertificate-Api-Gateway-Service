package ch.admin.bag.covidcertificate.gateway.client.eiam;

public enum QueryType {
    BY_USER_EXT_ID(""),
    BY_USER_CH_LOGIN_SUBJECT("E-ID CH-LOGIN"),
    BY_USER_HIN_LOGIN_SUBJECT("HIN");

    private final String idpSource;

    QueryType(String idpSource) {
        this.idpSource = idpSource;
    }

    public String getIdpSource() {
        return idpSource;
    }
}