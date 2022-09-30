package ch.admin.bag.covidcertificate.gateway.web.config;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class CNExtractorTest {

    @Test
    void extract_cn_with_dots() {
        assertThat(CNExtractor.extract("/C=CH/O=Admin/OU=BIT-EAM/CN=vmsoajenkins10p01.bfi.admin.ch"), is("vmsoajenkins10p01.bfi.admin.ch"));
    }

    @Test
    void extract_cn_with_empty_spaces() {
        assertThat(CNExtractor.extract("/C=CH/O=Admin/OU=BIT-EAM/CN=SPITAL AAAA BB"), is("SPITAL AAAA BB"));
    }

}
