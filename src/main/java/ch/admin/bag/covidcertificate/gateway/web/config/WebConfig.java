package ch.admin.bag.covidcertificate.gateway.web.config;

import ch.admin.bag.covidcertificate.gateway.filters.IntegrityFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<IntegrityFilter> integrityFilterRegistration() {
        FilterRegistrationBean<IntegrityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(integrityFilter());
        registration.addUrlPatterns("/api/v1/covidcertificate/*");
        registration.setName("integrityFilter");
        registration.setOrder(1);
        return registration;
    }

    public IntegrityFilter integrityFilter() {
        return new IntegrityFilter(new ObjectMapper());
    }

}