package ch.admin.bag.covidcertificate.gateway.domain;

import java.util.Arrays;
import java.util.Optional;

public enum TestType {
    PCR("LP6464-4", "pcr"),
    RAPID_TEST("LP217198-3", "rapid");

    public final String typeCode;
    public final String kpiValue;

    TestType(String typeCode, String kpiValue) {
        this.typeCode = typeCode;
        this.kpiValue = kpiValue;
    }

    public String getKpiValue(){
        return this.kpiValue;
    }

    public static Optional<TestType> findByTypeCode(String thatTypeCode){
        return Arrays.stream(TestType.values())
                .filter(testType -> testType.typeCode.equalsIgnoreCase(thatTypeCode))
                .findFirst();
    }
}