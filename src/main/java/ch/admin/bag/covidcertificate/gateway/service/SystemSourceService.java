package ch.admin.bag.covidcertificate.gateway.service;

import ch.admin.bag.covidcertificate.gateway.service.dto.incoming.SystemSource;
import ch.admin.bag.covidcertificate.gateway.web.config.CustomHeaderAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SystemSourceService {

    @Value("#{'${allowed-common-names-for-system-source}'.split(',')}")
    private List<String> allowedCommonNamesForSystemSource;

    public SystemSource getRelevantSystemSource(SystemSource systemSource) {
        SystemSource relevantSystemSource;
        if (systemSource != null) {
            log.debug("SystemSource set in request. Checking CommonName...");
            var commonName = ((CustomHeaderAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getId();
            if (allowedCommonNamesForSystemSource.contains(commonName) && SystemSource.ApiPlatform.equals(systemSource)) {
                log.debug("SystemSource set to ApiPlatform by {}", commonName);
                relevantSystemSource = SystemSource.ApiPlatform;
            } else {
                relevantSystemSource = SystemSource.ApiGateway;
            }
        } else {
            relevantSystemSource = SystemSource.ApiGateway;
        }
        return relevantSystemSource;
    }
}
