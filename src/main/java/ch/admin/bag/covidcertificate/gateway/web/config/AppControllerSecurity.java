package ch.admin.bag.covidcertificate.gateway.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AppControllerSecurity extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/api/v1/ping").and().
                authorizeRequests().anyRequest().permitAll();
    }
}
