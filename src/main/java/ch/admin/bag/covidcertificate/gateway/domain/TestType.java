package ch.admin.bag.covidcertificate.gateway.domain;

public enum TestType {
    PCR("LP6464-4"),
    RAPID_TEST("LP217198-3");

    public final String typeCode;

    TestType(String typeCode) {
        this.typeCode = typeCode;
    }
}