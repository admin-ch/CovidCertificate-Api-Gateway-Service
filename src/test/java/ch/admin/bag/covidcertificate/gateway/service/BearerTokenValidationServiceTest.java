package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.domain.OtpRevocation;
import ch.admin.bag.covidcertificate.gateway.util.CustomTokenProviderUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.error.ErrorList.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class BearerTokenValidationServiceTest {

    private static OtpRevocationService otpRevocationService;
    private static OtpRevocation otpRevocation;
    private static String revokedJti = "RevokedJti";

    private final String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbuDmx93wy1N9SHb2GVbqr6lkJk9jwxwzQlsXVnBdnzRnHaA0MCJRNCtjVy4f0qmAHQk4hMzJHrL57s3hAWVqId6PBQs974JQk6WCJN3/CpPrgkNZeifw6OpmqlcTd6zu5u0MbUs6Mh42j1RlyrO/NFyqL5Eg9hD5YcHt97GfV+nsVJvRgS4wMcu6ouaIUrDt6WZ/o7CC4v0nZeEQleX2gtgMqOSQfWWagu1ZwNQ5Hg4QNP5IysMZC7xzszvdl7W/LMPfAuuZUOg0AJMsAwmZThvxk/9o41SnJl6ed4qlZ4uOEZfBeZ5e0iEkwrAFwSnsyQH0IW3Wr/UskBrAg/x9rAgMBAAECggEAVyw6oDY7gPlKS136y0kSx0rZrVLnD2Ne+SZuebZ4I9PdqpPFOgdTfg2kdYsLARyfxXCI7G0MqLM7r2Q43U0oMV1Iftg37tE6Ha/IKwi2rPBOwYhTeXklijNj8usE2nblaIQ8fP9OQb1gvWZ+aIQHeniNiOKyzj1J6ZiOiV/egRpoT7+3sY6csX6uSO5/0r3rL7TsMgmn/mH4NwHm5UItrGmmKO4LR8cLiOmyfCbB+4/UjXj9JAmZDe7Nn+/W4H4wWWNk8MC79ke/3M5i9EG6hNF3AbRf5R2sMiMW59jN7AeRXGoiCOfrGXWNvE78+Pom2qhbdFFx2djtVK4YbSLVgQKBgQDMqKjlQLqdZ5fo2M49sGVSP1YuUlWbxj4BeJku/ZCO5DzZ4fU3v5VjWztFbhTdPVghbo1tGqEGSFZ/LAO7wWUGu0XKs/r01QACxSNcThB4X3/RjF2rwV+lLgCHoVctIP3roA+tOoszzwNxTqqXd08T8ckiW4+nf8Ft5EtFVvvLJQKBgQDCyKeJ7EcJNusZ2uIQic4gZjgOguXUDACC0Tn5wMyN81niCQugFJzqCkrYJABGPGWNEEFPbYiuSVyxvwZ37Z/Zi+3d+hDL74PLOz24z7CZK253oqFG9k3Ddvnd7bK+ZLt0dYF6t7hNHI4PPs3+Li/D/poIapzfLPCte2HJfyIDTwKBgGtVbTbGqtiQkxAQXKHn2Eu5YfZrQfCvmKdm21fUrjLyqqNOqS+yr6NrHnu8Tv71BDqMY2m8FIVZ/Ns3d0HKHLTaFLFJkS1EZHwPbgsj+elXlI6OwjWo9gOIS8jWKgVGD0W7LV2ZnZXvVQvgyQElFnkMToNRZ9bd3tFGcN+NzgJtAoGBAMETKI8ceCV4HH6aaq8+CeYvrK0lry8LXo5NWoxoQdsLNzNJCA77n7aV0S6CMQtt3rN/Q126E1u/OHSwB3dlQafgfj4kG/YqSpdu93Vz2Xdah7tqpzax+s8f5fnIHf9/1hhQSbIc3kEBZwdRl9q2aX57pq9lDm5iG4e632ld7ZcdAoGAIG6loMn5Qxp6O3DidxuUxkaQXCYM/WHfwp+kP5IRxAtCmb/nldgpebQtngC4vcWXdngRItdh1v9WX6aBWvwLkSdqI2HrL1AGssLvXU50FQGPkQSShXL0cItJg/fDKdP2Aw1+Q8+r2mhfd8TjMAxYgTxuYivck3FPzp2hI99A78I=";

    private final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm7g5sfd8MtTfUh29hlW6q+pZCZPY8McM0JbF1ZwXZ80Zx2gNDAiUTQrY1cuH9KpgB0JOITMyR6y+e7N4QFlaiHejwULPe+CUJOlgiTd/wqT64JDWXon8OjqZqpXE3es7ubtDG1LOjIeNo9UZcqzvzRcqi+RIPYQ+WHB7fexn1fp7FSb0YEuMDHLuqLmiFKw7elmf6OwguL9J2XhEJXl9oLYDKjkkH1lmoLtWcDUOR4OEDT+SMrDGQu8c7M73Ze1vyzD3wLrmVDoNACTLAMJmU4b8ZP/aONUpyZenneKpWeLjhGXwXmeXtIhJMKwBcEp7MkB9CFt1q/1LJAawIP8fawIDAQAB";

    @BeforeAll
    static void setUp() {
        otpRevocation = mock(OtpRevocation.class);
        otpRevocationService = mock(OtpRevocationService.class);
        when(otpRevocation.getJti()).thenReturn(revokedJti);
        when(otpRevocationService.getOtpRevocations()).thenReturn(List.of(otpRevocation));
    }

    @Test
    void validate_tokenIsOK() throws NoSuchAlgorithmException, InvalidBearerTokenException {

        //give
        CustomTokenProviderUtil customTokenProviderUtil = new CustomTokenProviderUtil(5000, privateKey, "test");

        BearerTokenValidationService service = new BearerTokenValidationService(otpRevocationService);
        ReflectionTestUtils.setField(service, "publicKey", publicKey);

        service.init();

        String token = customTokenProviderUtil.createToken("test", "test");

        service.validate(token);
    }

    @Test
    void invalid_tokenWrongScope() throws NoSuchAlgorithmException {
        //given
        String otherPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbuDmx93wy1N9SHb2GVbqr6lkJk9jwxwzQlsXVnBdnzRnHaA0MCJRNCtjVy4f0qmAHQk4hMzJHrL57s3hAWVqId6PBQs974JQk6WCJN3/CpPrgkNZeifw6OpmqlcTd6zu5u0MbUs6Mh42j1RlyrO/NFyqL5Eg9hD5YcHt97GfV+nsVJvRgS4wMcu6ouaIUrDt6WZ/o7CC4v0nZeEQleX2gtgMqOSQfWWagu1ZwNQ5Hg4QNP5IysMZC7xzszvdl7W/LMPfAuuZUOg0AJMsAwmZThvxk/9o41SnJl6ed4qlZ4uOEZfBeZ5e0iEkwrAFwSnsyQH0IW3Wr/UskBrAg/x9rAgMBAAECggEAVyw6oDY7gPlKS136y0kSx0rZrVLnD2Ne+SZuebZ4I9PdqpPFOgdTfg2kdYsLARyfxXCI7G0MqLM7r2Q43U0oMV1Iftg37tE6Ha/IKwi2rPBOwYhTeXklijNj8usE2nblaIQ8fP9OQb1gvWZ+aIQHeniNiOKyzj1J6ZiOiV/egRpoT7+3sY6csX6uSO5/0r3rL7TsMgmn/mH4NwHm5UItrGmmKO4LR8cLiOmyfCbB+4/UjXj9JAmZDe7Nn+/W4H4wWWNk8MC79ke/3M5i9EG6hNF3AbRf5R2sMiMW59jN7AeRXGoiCOfrGXWNvE78+Pom2qhbdFFx2djtVK4YbSLVgQKBgQDMqKjlQLqdZ5fo2M49sGVSP1YuUlWbxj4BeJku/ZCO5DzZ4fU3v5VjWztFbhTdPVghbo1tGqEGSFZ/LAO7wWUGu0XKs/r01QACxSNcThB4X3/RjF2rwV+lLgCHoVctIP3roA+tOoszzwNxTqqXd08T8ckiW4+nf8Ft5EtFVvvLJQKBgQDCyKeJ7EcJNusZ2uIQic4gZjgOguXUDACC0Tn5wMyN81niCQugFJzqCkrYJABGPGWNEEFPbYiuSVyxvwZ37Z/Zi+3d+hDL74PLOz24z7CZK253oqFG9k3Ddvnd7bK+ZLt0dYF6t7hNHI4PPs3+Li/D/poIapzfLPCte2HJfyIDTwKBgGtVbTbGqtiQkxAQXKHn2Eu5YfZrQfCvmKdm21fUrjLyqqNOqS+yr6NrHnu8Tv71BDqMY2m8FIVZ/Ns3d0HKHLTaFLFJkS1EZHwPbgsj+elXlI6OwjWo9gOIS8jWKgVGD0W7LV2ZnZXvVQvgyQElFnkMToNRZ9bd3tFGcN+NzgJtAoGBAMETKI8ceCV4HH6aaq8+CeYvrK0lry8LXo5NWoxoQdsLNzNJCA77n7aV0S6CMQtt3rN/Q126E1u/OHSwB3dlQafgfj4kG/YqSpdu93Vz2Xdah7tqpzax+s8f5fnIHf9/1hhQSbIc3kEBZwdRl9q2aX57pq9lDm5iG4e632ld7ZcdAoGAIG6loMn5Qxp6O3DidxuUxkaQXCYM/WHfwp+kP5IRxAtCmb/nldgpebQtngC4vcWXdngRItdh1v9WX6aBWvwLkSdqI2HrL1AGssLvXU50FQGPkQSShXL0cItJg/fDKdP2Aw1+Q8+r2mhfd8TjMAxYgTxuYivck3FPzp2hI99A78I=";

        CustomTokenProviderUtil customTokenProviderUtil = new CustomTokenProviderUtil(5000, otherPrivateKey, "test");
        ReflectionTestUtils.setField(CustomTokenProviderUtil.class, "SCOPE_CLAIM_KEY", "invalid");

        BearerTokenValidationService service = new BearerTokenValidationService(otpRevocationService);
        ReflectionTestUtils.setField(service, "publicKey", publicKey);

        service.init();

        String token = customTokenProviderUtil.createToken("test", "test");

        assertThrows(InvalidBearerTokenException.class, () -> service.validate(token));
    }

    @Test
    void invalid_tokenNoClaim() throws NoSuchAlgorithmException {
        //given
        String otherPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbuDmx93wy1N9SHb2GVbqr6lkJk9jwxwzQlsXVnBdnzRnHaA0MCJRNCtjVy4f0qmAHQk4hMzJHrL57s3hAWVqId6PBQs974JQk6WCJN3/CpPrgkNZeifw6OpmqlcTd6zu5u0MbUs6Mh42j1RlyrO/NFyqL5Eg9hD5YcHt97GfV+nsVJvRgS4wMcu6ouaIUrDt6WZ/o7CC4v0nZeEQleX2gtgMqOSQfWWagu1ZwNQ5Hg4QNP5IysMZC7xzszvdl7W/LMPfAuuZUOg0AJMsAwmZThvxk/9o41SnJl6ed4qlZ4uOEZfBeZ5e0iEkwrAFwSnsyQH0IW3Wr/UskBrAg/x9rAgMBAAECggEAVyw6oDY7gPlKS136y0kSx0rZrVLnD2Ne+SZuebZ4I9PdqpPFOgdTfg2kdYsLARyfxXCI7G0MqLM7r2Q43U0oMV1Iftg37tE6Ha/IKwi2rPBOwYhTeXklijNj8usE2nblaIQ8fP9OQb1gvWZ+aIQHeniNiOKyzj1J6ZiOiV/egRpoT7+3sY6csX6uSO5/0r3rL7TsMgmn/mH4NwHm5UItrGmmKO4LR8cLiOmyfCbB+4/UjXj9JAmZDe7Nn+/W4H4wWWNk8MC79ke/3M5i9EG6hNF3AbRf5R2sMiMW59jN7AeRXGoiCOfrGXWNvE78+Pom2qhbdFFx2djtVK4YbSLVgQKBgQDMqKjlQLqdZ5fo2M49sGVSP1YuUlWbxj4BeJku/ZCO5DzZ4fU3v5VjWztFbhTdPVghbo1tGqEGSFZ/LAO7wWUGu0XKs/r01QACxSNcThB4X3/RjF2rwV+lLgCHoVctIP3roA+tOoszzwNxTqqXd08T8ckiW4+nf8Ft5EtFVvvLJQKBgQDCyKeJ7EcJNusZ2uIQic4gZjgOguXUDACC0Tn5wMyN81niCQugFJzqCkrYJABGPGWNEEFPbYiuSVyxvwZ37Z/Zi+3d+hDL74PLOz24z7CZK253oqFG9k3Ddvnd7bK+ZLt0dYF6t7hNHI4PPs3+Li/D/poIapzfLPCte2HJfyIDTwKBgGtVbTbGqtiQkxAQXKHn2Eu5YfZrQfCvmKdm21fUrjLyqqNOqS+yr6NrHnu8Tv71BDqMY2m8FIVZ/Ns3d0HKHLTaFLFJkS1EZHwPbgsj+elXlI6OwjWo9gOIS8jWKgVGD0W7LV2ZnZXvVQvgyQElFnkMToNRZ9bd3tFGcN+NzgJtAoGBAMETKI8ceCV4HH6aaq8+CeYvrK0lry8LXo5NWoxoQdsLNzNJCA77n7aV0S6CMQtt3rN/Q126E1u/OHSwB3dlQafgfj4kG/YqSpdu93Vz2Xdah7tqpzax+s8f5fnIHf9/1hhQSbIc3kEBZwdRl9q2aX57pq9lDm5iG4e632ld7ZcdAoGAIG6loMn5Qxp6O3DidxuUxkaQXCYM/WHfwp+kP5IRxAtCmb/nldgpebQtngC4vcWXdngRItdh1v9WX6aBWvwLkSdqI2HrL1AGssLvXU50FQGPkQSShXL0cItJg/fDKdP2Aw1+Q8+r2mhfd8TjMAxYgTxuYivck3FPzp2hI99A78I=";

        CustomTokenProviderUtil customTokenProviderUtil = new CustomTokenProviderUtil(5000, otherPrivateKey, "test");

        BearerTokenValidationService service = new BearerTokenValidationService(otpRevocationService);
        ReflectionTestUtils.setField(service, "publicKey", publicKey);

        service.init();

        String tokenNoExtId = customTokenProviderUtil.createToken("", "test");

        assertThrows(InvalidBearerTokenException.class, () -> service.validate(tokenNoExtId));

        String tokenNoIdpSource = customTokenProviderUtil.createToken("test", "");

        assertThrows(InvalidBearerTokenException.class, () -> service.validate(tokenNoIdpSource));
    }

    @Test
    void invalid_tokenExpired() throws NoSuchAlgorithmException {
        //given
        String otherPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCbuDmx93wy1N9SHb2GVbqr6lkJk9jwxwzQlsXVnBdnzRnHaA0MCJRNCtjVy4f0qmAHQk4hMzJHrL57s3hAWVqId6PBQs974JQk6WCJN3/CpPrgkNZeifw6OpmqlcTd6zu5u0MbUs6Mh42j1RlyrO/NFyqL5Eg9hD5YcHt97GfV+nsVJvRgS4wMcu6ouaIUrDt6WZ/o7CC4v0nZeEQleX2gtgMqOSQfWWagu1ZwNQ5Hg4QNP5IysMZC7xzszvdl7W/LMPfAuuZUOg0AJMsAwmZThvxk/9o41SnJl6ed4qlZ4uOEZfBeZ5e0iEkwrAFwSnsyQH0IW3Wr/UskBrAg/x9rAgMBAAECggEAVyw6oDY7gPlKS136y0kSx0rZrVLnD2Ne+SZuebZ4I9PdqpPFOgdTfg2kdYsLARyfxXCI7G0MqLM7r2Q43U0oMV1Iftg37tE6Ha/IKwi2rPBOwYhTeXklijNj8usE2nblaIQ8fP9OQb1gvWZ+aIQHeniNiOKyzj1J6ZiOiV/egRpoT7+3sY6csX6uSO5/0r3rL7TsMgmn/mH4NwHm5UItrGmmKO4LR8cLiOmyfCbB+4/UjXj9JAmZDe7Nn+/W4H4wWWNk8MC79ke/3M5i9EG6hNF3AbRf5R2sMiMW59jN7AeRXGoiCOfrGXWNvE78+Pom2qhbdFFx2djtVK4YbSLVgQKBgQDMqKjlQLqdZ5fo2M49sGVSP1YuUlWbxj4BeJku/ZCO5DzZ4fU3v5VjWztFbhTdPVghbo1tGqEGSFZ/LAO7wWUGu0XKs/r01QACxSNcThB4X3/RjF2rwV+lLgCHoVctIP3roA+tOoszzwNxTqqXd08T8ckiW4+nf8Ft5EtFVvvLJQKBgQDCyKeJ7EcJNusZ2uIQic4gZjgOguXUDACC0Tn5wMyN81niCQugFJzqCkrYJABGPGWNEEFPbYiuSVyxvwZ37Z/Zi+3d+hDL74PLOz24z7CZK253oqFG9k3Ddvnd7bK+ZLt0dYF6t7hNHI4PPs3+Li/D/poIapzfLPCte2HJfyIDTwKBgGtVbTbGqtiQkxAQXKHn2Eu5YfZrQfCvmKdm21fUrjLyqqNOqS+yr6NrHnu8Tv71BDqMY2m8FIVZ/Ns3d0HKHLTaFLFJkS1EZHwPbgsj+elXlI6OwjWo9gOIS8jWKgVGD0W7LV2ZnZXvVQvgyQElFnkMToNRZ9bd3tFGcN+NzgJtAoGBAMETKI8ceCV4HH6aaq8+CeYvrK0lry8LXo5NWoxoQdsLNzNJCA77n7aV0S6CMQtt3rN/Q126E1u/OHSwB3dlQafgfj4kG/YqSpdu93Vz2Xdah7tqpzax+s8f5fnIHf9/1hhQSbIc3kEBZwdRl9q2aX57pq9lDm5iG4e632ld7ZcdAoGAIG6loMn5Qxp6O3DidxuUxkaQXCYM/WHfwp+kP5IRxAtCmb/nldgpebQtngC4vcWXdngRItdh1v9WX6aBWvwLkSdqI2HrL1AGssLvXU50FQGPkQSShXL0cItJg/fDKdP2Aw1+Q8+r2mhfd8TjMAxYgTxuYivck3FPzp2hI99A78I=";

        CustomTokenProviderUtil customTokenProviderUtil = new CustomTokenProviderUtil(0, otherPrivateKey, "test");

        BearerTokenValidationService service = new BearerTokenValidationService(otpRevocationService);
        ReflectionTestUtils.setField(service, "publicKey", publicKey);

        service.init();

        String token = customTokenProviderUtil.createToken("test", "test");

        assertThrows(InvalidBearerTokenException.class, () -> service.validate(token));
    }

    @Test
    void invalid_tokenNotSigned() throws NoSuchAlgorithmException {

        //give
        CustomTokenProviderUtil customTokenProviderUtil = new CustomTokenProviderUtil(5000, privateKey, "test");

        BearerTokenValidationService service = new BearerTokenValidationService(otpRevocationService);

        ReflectionTestUtils.setField(service, "publicKey", publicKey);

        service.init();

        String token = customTokenProviderUtil.createTokenNotSigned("test", "test");

        assertThrows(InvalidBearerTokenException.class, () -> service.validate(token));
    }

    @Test
    void throwsException__ifInvalidLength() throws NoSuchAlgorithmException {
        //give
        CustomTokenProviderUtil customTokenProviderUtil = new CustomTokenProviderUtil(5000, privateKey, "test");

        BearerTokenValidationService service = new BearerTokenValidationService(otpRevocationService);
        ReflectionTestUtils.setField(service, "publicKey", publicKey);

        service.init();

        String token = customTokenProviderUtil.createToken("test", "test");

        final String tokenTruncatedEnd = token.substring(0, token.length() - 1);
        InvalidBearerTokenException exception = assertThrows(InvalidBearerTokenException.class, () -> service.validate(tokenTruncatedEnd));
        var restError = exception.getError();
        assertEquals(INVALID_OTP_LENGTH_MESSAGE, restError.getErrorMessage());
        assertEquals(INVALID_OTP_LENGTH_CODE, restError.getErrorCode());

        final String tokenTruncatedStart = token.substring(1);
        exception = assertThrows(InvalidBearerTokenException.class, () -> service.validate(tokenTruncatedStart));
        restError = exception.getError();
        assertEquals(INVALID_OTP_LENGTH_MESSAGE, restError.getErrorMessage());
        assertEquals(INVALID_OTP_LENGTH_CODE, restError.getErrorCode());
    }

    @Test
    void invalid_otpRevoked() throws NoSuchAlgorithmException {
        //give
        CustomTokenProviderUtil customTokenProviderUtil = new CustomTokenProviderUtil(5000, privateKey, "test");

        BearerTokenValidationService service = new BearerTokenValidationService(otpRevocationService);
        ReflectionTestUtils.setField(service, "publicKey", publicKey);

        service.init();

        String token = customTokenProviderUtil.createToken("test", "test", revokedJti);

        InvalidBearerTokenException exception = assertThrows(InvalidBearerTokenException.class, () -> service.validate(token));
        assertEquals(INVALID_BEARER, exception.getError());
    }
}
