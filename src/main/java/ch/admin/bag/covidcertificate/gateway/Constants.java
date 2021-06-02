package ch.admin.bag.covidcertificate.gateway;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    public static final String KPI_UUID_KEY = "uuid";
    public static final String KPI_TIMESTAMP_KEY = "ts";
    public static final String KPI_TYPE_KEY = "type";
    public static final String KPI_CERT_KEY = "cert";
    public static final String KPI_CREATE_CERTIFICATE_TYPE = "cc";
    public static final String KPI_REVOKE_CERTIFICATE_TYPE = "re";
    public static final String KPI_COMMON_NAME_TYPE = "cn";
    public static final String KPI_SYSTEM_API = "api";
    public static final DateTimeFormatter LOG_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final String KPI_TYPE_VACCINATION = "v";
    public static final String KPI_TYPE_TEST = "t";
    public static final String KPI_TYPE_RECOVERY = "r";
}
