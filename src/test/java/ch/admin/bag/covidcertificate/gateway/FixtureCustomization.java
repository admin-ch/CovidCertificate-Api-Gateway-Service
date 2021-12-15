package ch.admin.bag.covidcertificate.gateway;

import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.*;
import com.flextrade.jfixture.JFixture;
import org.springframework.test.util.ReflectionTestUtils;

public class FixtureCustomization {

    public static void customizeVaccinationCertificateCreateDto(JFixture fixture) {
        fixture.customise().lazyInstance(VaccinationCertificateCreateDto.class, () -> {
            var helperFixture = new JFixture();
            helperFixture.customise().repeatCount(1);
            customizeVaccinationCertificateDataDto(helperFixture);
            var vaccinationCertificateCreateDto = helperFixture.create(VaccinationCertificateCreateDto.class);
            ReflectionTestUtils.setField(vaccinationCertificateCreateDto, "language", "de");
            ReflectionTestUtils.setField(vaccinationCertificateCreateDto, "personData", helperFixture.create(CovidCertificatePersonDto.class));
            ReflectionTestUtils.setField(vaccinationCertificateCreateDto, "otp", fixture.create(String.class));
            return vaccinationCertificateCreateDto;
        });
    }

    private static void customizeVaccinationCertificateDataDto(JFixture fixture) {
        fixture.customise().lazyInstance(VaccinationCertificateDataDto.class, () -> {
            var numberOfDoses = fixture.create(Integer.class) % 9 + 1;
            var totalNumberOfDoses = numberOfDoses + (int) Math.ceil(Math.random() * (9 - numberOfDoses));
            var vaccinationCertificateCreateDto = new JFixture().create(VaccinationCertificateDataDto.class);
            ReflectionTestUtils.setField(vaccinationCertificateCreateDto, "numberOfDoses", numberOfDoses);
            ReflectionTestUtils.setField(vaccinationCertificateCreateDto, "totalNumberOfDoses", totalNumberOfDoses);
            return vaccinationCertificateCreateDto;
        });
    }

    public static void customizeVaccinationTouristCertificateCreateDto(JFixture fixture) {
        fixture.customise().lazyInstance(VaccinationTouristCertificateCreateDto.class, () -> {
            var helperFixture = new JFixture();
            helperFixture.customise().repeatCount(1);
            customizeVaccinationTouristCertificateDataDto(helperFixture);
            var vaccinationTouristCertificateCreateDto = helperFixture.create(VaccinationTouristCertificateCreateDto.class);
            ReflectionTestUtils.setField(vaccinationTouristCertificateCreateDto, "language", "de");
            ReflectionTestUtils.setField(vaccinationTouristCertificateCreateDto, "personData", helperFixture.create(CovidCertificatePersonDto.class));
            ReflectionTestUtils.setField(vaccinationTouristCertificateCreateDto, "otp", fixture.create(String.class));
            return vaccinationTouristCertificateCreateDto;
        });
    }

    private static void customizeVaccinationTouristCertificateDataDto(JFixture fixture) {
        fixture.customise().lazyInstance(VaccinationTouristCertificateDataDto.class, () -> {
            var numberOfDoses = fixture.create(Integer.class) % 9 + 1;
            var totalNumberOfDoses = numberOfDoses + (int) Math.ceil(Math.random() * (9 - numberOfDoses));
            var vaccinationTouristCertificateCreateDto = new JFixture().create(VaccinationTouristCertificateDataDto.class);
            ReflectionTestUtils.setField(vaccinationTouristCertificateCreateDto, "numberOfDoses", numberOfDoses);
            ReflectionTestUtils.setField(vaccinationTouristCertificateCreateDto, "totalNumberOfDoses", totalNumberOfDoses);
            return vaccinationTouristCertificateCreateDto;
        });
    }

    public static void customizeTestCertificateCreateDto(JFixture fixture) {
        fixture.customise().lazyInstance(TestCertificateCreateDto.class, () -> {
            var helperFixture = new JFixture();
            helperFixture.customise().repeatCount(1);
            var testCertificateCreateDto = helperFixture.create(TestCertificateCreateDto.class);
            ReflectionTestUtils.setField(testCertificateCreateDto, "language", "de");
            ReflectionTestUtils.setField(testCertificateCreateDto, "personData", helperFixture.create(CovidCertificatePersonDto.class));
            ReflectionTestUtils.setField(testCertificateCreateDto, "otp", fixture.create(String.class));
            return testCertificateCreateDto;
        });
    }

    public static void customizeRecoveryCertificateCreateDto(JFixture fixture) {
        fixture.customise().lazyInstance(RecoveryCertificateCreateDto.class, () -> {
            var helperFixture = new JFixture();
            helperFixture.customise().repeatCount(1);
            var recoveryCertificateCreateDto = helperFixture.create(RecoveryCertificateCreateDto.class);
            ReflectionTestUtils.setField(recoveryCertificateCreateDto, "language", "de");
            ReflectionTestUtils.setField(recoveryCertificateCreateDto, "personData", helperFixture.create(CovidCertificatePersonDto.class));
            ReflectionTestUtils.setField(recoveryCertificateCreateDto, "otp", fixture.create(String.class));
            return recoveryCertificateCreateDto;
        });
    }

    public static void customizeAntibodyCertificateCreateDto(JFixture fixture) {
        fixture.customise().lazyInstance(AntibodyCertificateCreateDto.class, () -> {
            var helperFixture = new JFixture();
            helperFixture.customise().repeatCount(1);
            var recoveryCertificateCreateDto = helperFixture.create(AntibodyCertificateCreateDto.class);
            ReflectionTestUtils.setField(recoveryCertificateCreateDto, "language", "de");
            ReflectionTestUtils.setField(recoveryCertificateCreateDto, "personData", helperFixture.create(CovidCertificatePersonDto.class));
            ReflectionTestUtils.setField(recoveryCertificateCreateDto, "otp", fixture.create(String.class));
            return recoveryCertificateCreateDto;
        });
    }

}
