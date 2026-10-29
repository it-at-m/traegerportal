package de.muenchen.rbs.traegerportal.gateway.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.session.autoconfigure.SessionProperties;
import org.springframework.boot.session.autoconfigure.SessionTimeout;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import java.time.Duration;

@Slf4j
@Configuration
@Profile("!no-security")
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final SessionTimeout sessionTimeout;

    @Bean
    @Order(1)
    public SecurityWebFilterChain webcomponentsFilterChain(final ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/webcomponents/**"
                ))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain meinTraegerApiFilterChain(final ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/meintraeger/**",
                        "/meinevorgaenge/**"
                ))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }

    @Bean
    @Order(3)
    public SecurityWebFilterChain directApiFilterChain(final ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/traeger/by-unternehmenskontoid/**"
                ))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange()
                        .hasAuthority("CLIENT_VALIDIERUNGSEAI")
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }

    @Bean
    @Order(4)
    public SecurityWebFilterChain clientAccessFilterChain(
            final ServerHttpSecurity http,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") final String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") final String jwkSetUri) {
        log.info("Initializing security with issuer URI {} and jwk set uri {}", issuerUri, jwkSetUri);

        // security config
        return http.
                securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/**"
                ))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/api/*/actuator/info",
                                "/actuator/health",
                                "/actuator/health/liveness",
                                "/actuator/health/readiness",
                                "/actuator/info",
                                "/actuator/metrics",
                                "/actuator/sbom",
                                "/actuator/sbom/application")
                        .permitAll()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .build();
    }

    /**
     * Returns the session timeout determined by Spring Boot.
     * <p>
     * Prior to Spring Boot 4, the timeout was resolved explicitly using {@link SessionProperties} with {@link ServerProperties#getServlet()} as a fallback,
     * mirroring the timeout resolution described in the
     * <a href="https://docs.spring.io/spring-boot/reference/web/spring-session.html">Spring
     * Session</a> documentation.
     * <p>
     * Since Spring Boot 4, this resolution is encapsulated by {@link SessionTimeout}. An {@link IllegalStateException} is thrown if no session timeout could be
     * determined.
     *
     * @return the resolved session timeout
     * @throws IllegalStateException if no session timeout could be determined
     */
    protected Duration getSessionTimeout() {
        final Duration timeout = sessionTimeout.getTimeout();
        if (timeout == null) {
            throw new IllegalStateException("Unable to determine the session timeout.");
        }
        return timeout;
    }
}
