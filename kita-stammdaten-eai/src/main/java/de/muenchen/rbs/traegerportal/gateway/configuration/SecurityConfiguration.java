package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.session.autoconfigure.SessionProperties;
import org.springframework.boot.session.autoconfigure.SessionTimeout;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
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

    private final SessionTimeout sessionTimeout;

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
                                    "/actuator/sbom/application",
                                    "/webcomponents/**")
                                    .permitAll();
                            authorizeExchangeSpec.anyExchange().authenticated();
                        })
                .cors(corsSpec -> {
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Returns the session timeout determined by Spring Boot.
     * <p>
     * Prior to Spring Boot 4, the timeout was resolved explicitly using {@link SessionProperties} with {@link ServerProperties#getServlet()} as a fallback,
     * mirroring the timeout resolution described in the <a href="https://docs.spring.io/spring-boot/reference/web/spring-session.html">Spring Session</a>
     * documentation.
     * <p>
     * Since Spring Boot 4, this resolution is encapsulated by {@link SessionTimeout}. An {@link IllegalStateException} is thrown if no session timeout could be
     * determined.
     *
     * @return the resolved session timeout
     * @throws IllegalStateException if no session timeout could be determined
     */
    protected Duration getSessionTimeout() {
        Duration timeout = sessionTimeout.getTimeout();
        if (timeout == null) {
            throw new IllegalStateException("Unable to determine the session timeout.");
        }
        return timeout;
    }
}
