package ch.admin.bag.covidcertificate.gateway.web.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static ch.admin.bag.covidcertificate.gateway.Constants.*;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RequiredArgsConstructor
public class CustomHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final List<String> allowedCommonNames;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("X-Client-DN");

        if (!StringUtils.hasText(authorizationHeader)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            log.error("authorization header not present");
            return;
        }

        log.debug("Header Authorization: {}", authorizationHeader);

        String commonName = CNExtractor.extract(authorizationHeader);

        log.info("Found '{}' from '{}'", kv("commonName", commonName), authorizationHeader);

        //TODO: Reactivate Check when the list is updated
//        if (!allowedCommonNames.contains(commonName)) {
//            response.setStatus(HttpStatus.FORBIDDEN.value());
//            log.error("Common-Name '{}' not allowed. Please register your certificate.", commonName);
//            return;
//        }

        String clientCert = request.getHeader("X-Client-Cert");

        if (!StringUtils.hasText(clientCert)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            log.error("clientCert not present");
            return;
        }

        log.info("Found clientCert: '{}'", clientCert);

        Authentication auth = new CustomHeaderAuthenticationToken(commonName, clientCert);
        SecurityContextHolder.getContext().setAuthentication(auth);

        logKpi(commonName);

        filterChain.doFilter(request, response);
    }

    private void logKpi(String commonName) {
        log.info("kpi: {} {}", kv(KPI_TIMESTAMP_KEY, ZonedDateTime.now(SWISS_TIMEZONE).format(LOG_FORMAT)), kv(KPI_CERT_KEY, commonName));
    }
}
