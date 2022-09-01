package ch.admin.bag.covidcertificate.gateway;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final String KPI_TIMESTAMP_KEY = "ts";
    public static final String KPI_CERT_KEY = "cert";
    public static final String KPI_CREATE_CERTIFICATE_TYPE = "cc";
    public static final String KPI_REVOKE_CERTIFICATE_TYPE = "re";
    public static final String KPI_COMMON_NAME_TYPE = "cn";
    public static final String KPI_SYSTEM_API = "api";
    public static final DateTimeFormatter LOG_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String KPI_TYPE_VACCINATION = "v";
    public static final String KPI_TYPE_VACCINATION_TOURIST = "vt";
    public static final String KPI_TYPE_TEST = "t";
    public static final String KPI_TYPE_RECOVERY = "r";
    public static final String KPI_TYPE_RECOVERY_RAT = "rr";
    public static final String KPI_TYPE_ANTIBODY = "a";
    public static final String SEC_KPI_EXT_ID = "extId";
    public static final String SEC_KPI_IP_ADDRESS = "ip";
    public static final String SEC_KPI_IDP_SOURCE = "idp";
    public static final String SEC_KPI_OTP_JWT_ID = "jwtId";
    public static final String SEC_KPI_OTP_TYPE = "otpType";

    public static final String UUID_CLAIM_KEY = "uuid";
    public static final String IDP_SOURCE_CLAIM_KEY = "idpsource";
    public static final String CLIENT_NAME_KEY = "clientName";
}
