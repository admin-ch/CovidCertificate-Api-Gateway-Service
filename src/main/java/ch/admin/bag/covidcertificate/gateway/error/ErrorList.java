package ch.admin.bag.covidcertificate.gateway.error;

import ch.admin.bag.covidcertificate.gateway.filters.CustomHttpRequestWrapper;
import org.springframework.http.HttpStatus;

public class ErrorList {
    private ErrorList() {
    }

    private static final String ERROR_CODE_KEY = "{\"errorCode\": ";
    private static final String ERROR_MESSAGE_KEY = " \"errorMessage\": ";

    // Validation
    public static final String NO_VACCINATION_DATA = ERROR_CODE_KEY + 451 + ","
            + ERROR_MESSAGE_KEY + "No vaccination data was specified}";
    public static final String NO_PERSON_DATA = ERROR_CODE_KEY + 452 + ","
            + ERROR_MESSAGE_KEY + "No person data was specified}";
    public static final String INVALID_DATE_OF_BIRTH = ERROR_CODE_KEY + 453 + ","
            + ERROR_MESSAGE_KEY + "Invalid dateOfBirth! Must be younger than 1900-01-01}";
    public static final String INVALID_MEDICINAL_PRODUCT = ERROR_CODE_KEY + 454 + ","
            + ERROR_MESSAGE_KEY + "Invalid medicinal product}";
    public static final String INVALID_DOSES = ERROR_CODE_KEY + 455 + ","
            + ERROR_MESSAGE_KEY + "Invalid number of doses}";
    public static final String INVALID_VACCINATION_DATE = ERROR_CODE_KEY + 456 + ","
            + ERROR_MESSAGE_KEY + "Invalid vaccination date! Date cannot be in the future}";
    public static final String INVALID_COUNTRY_OF_VACCINATION = ERROR_CODE_KEY + 457 + ","
            + ERROR_MESSAGE_KEY + "Invalid country of vaccination}";
    public static final String INVALID_GIVEN_NAME = ERROR_CODE_KEY + 458 + ","
            + ERROR_MESSAGE_KEY + "Invalid given name! Must not exceed 50 chars}";
    public static final String INVALID_FAMILY_NAME = ERROR_CODE_KEY + 459 + ","
            + ERROR_MESSAGE_KEY + "Invalid family name! Must not exceed 50 chars}";
    public static final String NO_TEST_DATA = ERROR_CODE_KEY + 460 + ","
            + ERROR_MESSAGE_KEY + "No test data was specified}";
    public static final String INVALID_MEMBER_STATE_OF_TEST = ERROR_CODE_KEY + 461 + ","
            + ERROR_MESSAGE_KEY + "Invalid member state of test}";
    public static final String INVALID_TYP_OF_TEST = ERROR_CODE_KEY + 462 + ","
            + ERROR_MESSAGE_KEY + "Invalid type of test and manufacturer code combination! Must either be a PCR Test type and no manufacturer code or give a manufacturer code and the antigen test type code.}";
    public static final String INVALID_TEST_CENTER = ERROR_CODE_KEY + 463 + ","
            + ERROR_MESSAGE_KEY + "Invalid testing center or facility}";
    public static final String INVALID_SAMPLE_OR_RESULT_DATE_TIME = ERROR_CODE_KEY + 464 + ","
            + ERROR_MESSAGE_KEY + "Invalid sample or result date time! Sample date must be before current date and before result date}";
    public static final String NO_RECOVERY_DATA = ERROR_CODE_KEY + 465 + ","
            + ERROR_MESSAGE_KEY + "No recovery data specified}";
    public static final String INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT = ERROR_CODE_KEY + 466 + ","
            + ERROR_MESSAGE_KEY + "Invalid date of first positive test result}";
    public static final String INVALID_COUNTRY_OF_TEST = ERROR_CODE_KEY + 467 + ","
            + ERROR_MESSAGE_KEY + "Invalid country of test}";
    public static final String INVALID_COUNTRY_SHORT_FORM = ERROR_CODE_KEY + 468 + ","
            + ERROR_MESSAGE_KEY + "Country short form can not be mapped}";
    public static final String INVALID_LANGUAGE = ERROR_CODE_KEY + 469 + ","
            + ERROR_MESSAGE_KEY + "The given language does not match any of the supported languages: de, it, fr!}";
    public static final String INVALID_UVCI = ERROR_CODE_KEY + 470 + ","
            + ERROR_MESSAGE_KEY + "Invalid UVCI format.}";

    // Authorization
    private static final int INVALID_SIGNATURE_CODE = 490;
    private static final String INVALID_SIGNATURE_MESSAGE = "Integrity check failed, the body hash does not match the hash in the header";
    public static final RestError INVALID_SIGNATURE = new RestError(INVALID_SIGNATURE_CODE, INVALID_SIGNATURE_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String INVALID_SIGNATURE_JSON = ERROR_CODE_KEY + INVALID_SIGNATURE_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_SIGNATURE_MESSAGE + "}";

    private static final int SIGNATURE_PARSE_CODE = 491;
    private static final String SIGNATURE_PARSE_MESSAGE = "Signature could not be parsed";
    public static final RestError SIGNATURE_PARSE = new RestError(SIGNATURE_PARSE_CODE, SIGNATURE_PARSE_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String SIGNATURE_PARSE_JSON = ERROR_CODE_KEY + SIGNATURE_PARSE_CODE + ","
            + ERROR_MESSAGE_KEY + SIGNATURE_PARSE_MESSAGE + "}";

    public static final int INVALID_BEARER_CODE = 492;
    public static final String INVALID_BEARER_MESSAGE = "Invalid or missing bearer token";
    public static final RestError INVALID_BEARER = new RestError(INVALID_BEARER_CODE, INVALID_BEARER_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String INVALID_BEARER_JSON = ERROR_CODE_KEY + INVALID_BEARER_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_BEARER_MESSAGE + "}";

    private static final int PAYLOAD_TOO_LARGE_CODE = 493;
    private static final String PAYLOAD_TOO_LARGE_MESSAGE = "Request payload too large, the maximum payload size is: " + CustomHttpRequestWrapper.MAX_BODY_SIZE + " bytes";
    public static final RestError PAYLOAD_TOO_LARGE = new RestError(PAYLOAD_TOO_LARGE_CODE, PAYLOAD_TOO_LARGE_MESSAGE, HttpStatus.PAYLOAD_TOO_LARGE);
    public static final String PAYLOAD_TOO_LARGE_JSON = ERROR_CODE_KEY + PAYLOAD_TOO_LARGE_CODE + ","
            + ERROR_MESSAGE_KEY + PAYLOAD_TOO_LARGE_MESSAGE + "}";

    // Conflict
    public static final String DUPLICATE_UVCI = ERROR_CODE_KEY + 480 + ","
            + ERROR_MESSAGE_KEY + "Duplicate UVCI.}";
}
