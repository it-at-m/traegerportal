package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@Profile("!no-security")
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final SessionProperties sessionProperties;
    private final ServerProperties serverProperties;

    @Bean
    public SecurityWebFilterChain clientAccessFilterChain(final ServerHttpSecurity http) {
        // security config
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/**"))
                .authorizeExchange(
                        authorizeExchangeSpec -> {
                            authorizeExchangeSpec
                                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                            authorizeExchangeSpec.pathMatchers(
                                    "/api/*/actuator/info",
                                    "/actuator/health",
                                    "/actuator/health/liveness",
                                    "/actuator/health/readiness",
                                    "/actuator/info",
                                    "/actuator/metrics",
                                    "/actuator/sbom",
                                    "/actuator/sbom/application")
                                    .permitAll();
                            authorizeExchangeSpec.anyExchange().authenticated();
                        })
                .cors(corsSpec -> {
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Get Spring Session timeout. Uses {@link SessionProperties} and
     * {@link ServerProperties#getServlet()} as fallback, like Spring Session
     * itself. See according <a href=
     * "https://docs.spring.io/spring-boot/reference/web/spring-session.html">Spring
     * documentation</a>.
     *
     * @return Spring session timeout.
     */
    protected Duration getSessionTimeout() {
        return sessionProperties.determineTimeout(() -> serverProperties.getServlet().getSession().getTimeout());
    }
}
