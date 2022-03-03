package ch.admin.bag.covidcertificate.gateway.features.authorization.model;

public enum Function {
    CREATE_VACCINE_CERTIFICATE,
    CREATE_VACCINE_TOURIST_CERTIFICATE,
    CREATE_TEST_CERTIFICATE,
    CREATE_ANTIBODY_CERTIFICATE,
    CREATE_RECOVERY_CERTIFICATE,
    CREATE_RECOVERY_RAT_CERTIFICATE,
    CREATE_EXCEPTIONAL_CERTIFICATE,
    REVOKE_CERTIFICATE,
    REVOKE_CERTIFICATE_MASS;

    public String getIdentifier() {
        return this.name().toLowerCase().replace("_", "-");
    }
}