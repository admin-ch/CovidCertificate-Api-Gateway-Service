package ch.admin.bag.covidcertificate.gateway.error;

import org.springframework.http.HttpStatus;

public class ErrorList {
    private ErrorList() {
    }

    private static final String ERROR_CODE_KEY = "{\"errorCode\": ";
    private static final String ERROR_MESSAGE_KEY = " \"errorMessage\": ";

    // Validation
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
            + ERROR_MESSAGE_KEY + "Invalid given name! Must not exceed 80 chars}";
    public static final String INVALID_FAMILY_NAME = ERROR_CODE_KEY + 459 + ","
            + ERROR_MESSAGE_KEY + "Invalid family name! Must not exceed 80 chars}";
    public static final String INVALID_MEMBER_STATE_OF_TEST = ERROR_CODE_KEY + 461 + ","
            + ERROR_MESSAGE_KEY + "Invalid member state of test}";
    public static final String INVALID_TYP_OF_TEST = ERROR_CODE_KEY + 462 + ","
            + ERROR_MESSAGE_KEY + "Invalid type of test and manufacturer code combination! Must either be a PCR Test type and no manufacturer code or give a manufacturer code and the antigen test type code.}";
    public static final String INVALID_TEST_CENTER = ERROR_CODE_KEY + 463 + ","
            + ERROR_MESSAGE_KEY + "Invalid testing center or facility}";
    public static final String INVALID_SAMPLE_OR_RESULT_DATE_TIME = ERROR_CODE_KEY + 464 + ","
            + ERROR_MESSAGE_KEY + "Invalid sample or result date time! Sample date must be before current date and before result date}";
    public static final String INVALID_DATE_OF_FIRST_POSITIVE_TEST_RESULT = ERROR_CODE_KEY + 466 + ","
            + ERROR_MESSAGE_KEY + "Invalid date of first positive test result}";
    public static final String INVALID_COUNTRY_OF_TEST = ERROR_CODE_KEY + 467 + ","
            + ERROR_MESSAGE_KEY + "Invalid country of test}";
    public static final String INVALID_LANGUAGE = ERROR_CODE_KEY + 469 + ","
            + ERROR_MESSAGE_KEY + "The given language does not match any of the supported languages: de, it, fr!}";
    public static final String INVALID_UVCI = ERROR_CODE_KEY + 470 + ","
            + ERROR_MESSAGE_KEY + "Invalid UVCI format.}";
    public static final int INVALID_VACCINATION_INFO_CODE = 451;
    public static final String INVALID_VACCINATION_INFO_MESSAGE = "Invalid vaccination info! Exactly one element is expected in the array.";
    public static final RestError INVALID_VACCINATION_INFO = new RestError(INVALID_VACCINATION_INFO_CODE, INVALID_VACCINATION_INFO_MESSAGE, HttpStatus.BAD_REQUEST);
    public static final String INVALID_VACCINATION_INFO_JSON = ERROR_CODE_KEY + INVALID_VACCINATION_INFO_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_VACCINATION_INFO_MESSAGE + "}";
    public static final int INVALID_TEST_INFO_CODE = 460;
    public static final String INVALID_TEST_INFO_MESSAGE = "Invalid test info! Exactly one element is expected in the array.";
    public static final RestError INVALID_TEST_INFO = new RestError(INVALID_TEST_INFO_CODE, INVALID_TEST_INFO_MESSAGE, HttpStatus.BAD_REQUEST);
    public static final String INVALID_TEST_INFO_JSON = ERROR_CODE_KEY + INVALID_TEST_INFO_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_TEST_INFO_MESSAGE + "}";
    public static final int INVALID_RECOVERY_INFO_CODE = 465;
    public static final String INVALID_RECOVERY_INFO_MESSAGE = "Invalid recovery info! Exactly one element is expected in the array.";
    public static final RestError INVALID_RECOVERY_INFO = new RestError(INVALID_RECOVERY_INFO_CODE, INVALID_RECOVERY_INFO_MESSAGE, HttpStatus.BAD_REQUEST);
    public static final String INVALID_RECOVERY_INFO_JSON = ERROR_CODE_KEY + INVALID_RECOVERY_INFO_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_RECOVERY_INFO_MESSAGE + "}";
    public static final String INVALID_ADDRESS = ERROR_CODE_KEY + 474 + "," + ERROR_MESSAGE_KEY + "Paper-based delivery requires a valid address.}";
    public static final String DUPLICATE_DELIVERY_METHOD = ERROR_CODE_KEY + 475 + "," + ERROR_MESSAGE_KEY + "Delivery method can either be InApp or Mail, but not both.}";
    public static final String UNKNOWN_APP_CODE = ERROR_CODE_KEY + 476 + "," + ERROR_MESSAGE_KEY + "Unknown or invalid app code.}";
    public static final String INVALID_STANDARDISED_GIVEN_NAME = ERROR_CODE_KEY + 477 + "," + ERROR_MESSAGE_KEY + "Invalid given name! The standardised given name exceeds 80 chars}";
    public static final String INVALID_STANDARDISED_FAMILY_NAME = ERROR_CODE_KEY + 478 + "," + ERROR_MESSAGE_KEY + "Invalid family name! The standardised family name exceeds 80 chars}";
    public static final String INVALID_APP_CODE = ERROR_CODE_KEY + 479 + "," + ERROR_MESSAGE_KEY + "App code is in an invalid format.}";
    public static final String INVALID_PRINT_FOR_TEST = ERROR_CODE_KEY + 488 + "," + ERROR_MESSAGE_KEY + "Print is not available for test certificates}";
    public static final String INVALID_DATE_OF_BIRTH_IN_FUTURE = ERROR_CODE_KEY + 489 + "," + ERROR_MESSAGE_KEY + "Invalid dateOfBirth! Date cannot be in the future}";
    public static final int INVALID_ANTIBODY_INFO_CODE = 497;
    public static final String INVALID_ANTIBODY_INFO_MESSAGE = "Invalid antibody info! Exactly one element is expected in the array.";
    public static final RestError INVALID_ANTIBODY_INFO = new RestError(INVALID_ANTIBODY_INFO_CODE, INVALID_ANTIBODY_INFO_MESSAGE, HttpStatus.BAD_REQUEST);
    public static final String INVALID_ANTIBODY_INFO_JSON = ERROR_CODE_KEY + INVALID_ANTIBODY_INFO_CODE + "," + ERROR_MESSAGE_KEY + INVALID_ANTIBODY_INFO_MESSAGE + "}";

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
    public static final String INVALID_BEARER_MESSAGE = "Invalid OTP";
    public static final RestError INVALID_BEARER = new RestError(INVALID_BEARER_CODE, INVALID_BEARER_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String INVALID_BEARER_JSON = ERROR_CODE_KEY + INVALID_BEARER_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_BEARER_MESSAGE + "}";

    public static final int MISSING_BEARER_CODE = 493;
    public static final String MISSING_BEARER_MESSAGE = "Missing OTP";
    public static final RestError MISSING_BEARER = new RestError(MISSING_BEARER_CODE, MISSING_BEARER_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String MISSING_BEARER_JSON = ERROR_CODE_KEY + MISSING_BEARER_CODE + ","
            + ERROR_MESSAGE_KEY + MISSING_BEARER_MESSAGE + "}";

    public static final int INVALID_IDENTITY_USER_CODE = 494;
    public static final String INVALID_IDENTITY_USER_MESSAGE = "Invalid identity user";
    public static final RestError INVALID_IDENTITY_USER = new RestError(INVALID_IDENTITY_USER_CODE, INVALID_IDENTITY_USER_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String INVALID_IDENTITY_USER_JSON = ERROR_CODE_KEY + INVALID_IDENTITY_USER_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_IDENTITY_USER_MESSAGE + "}";

    public static final int INVALID_IDENTITY_USER_ROLE_CODE = 495;
    public static final String INVALID_IDENTITY_USER_ROLE_MESSAGE = "Invalid identity user role";
    public static final RestError INVALID_IDENTITY_USER_ROLE = new RestError(INVALID_IDENTITY_USER_ROLE_CODE, INVALID_IDENTITY_USER_ROLE_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String INVALID_IDENTITY_USER_ROLE_JSON = ERROR_CODE_KEY + INVALID_IDENTITY_USER_ROLE_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_IDENTITY_USER_ROLE_MESSAGE + "}";

    public static final int INVALID_OTP_LENGTH_CODE = 496;
    public static final String INVALID_OTP_LENGTH_MESSAGE = "Invalid OTP length";
    public static final RestError INVALID_OTP_LENGTH = new RestError(INVALID_OTP_LENGTH_CODE, INVALID_OTP_LENGTH_MESSAGE, HttpStatus.FORBIDDEN);
    public static final String INVALID_OTP_LENGTH_JSON = ERROR_CODE_KEY + INVALID_OTP_LENGTH_CODE + ","
            + ERROR_MESSAGE_KEY + INVALID_OTP_LENGTH_MESSAGE + "}";

    // Conflict
    public static final String DUPLICATE_UVCI = ERROR_CODE_KEY + 480 + ","
            + ERROR_MESSAGE_KEY + "Duplicate UVCI.}";
}
