package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.time.Duration;
import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@Profile("!no-security")
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final SessionTimeout sessionTimeout;

    @Bean
    @Order(1)
    public SecurityWebFilterChain webcomponentsFilterChain(final ServerHttpSecurity http) {
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/webcomponents/**"))
                .authorizeExchange(
                        authorizeExchangeSpec -> {
                            authorizeExchangeSpec.anyExchange().permitAll();
                        })
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain apiFilterChain(final ServerHttpSecurity http) {
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/meintraeger/**"))
                .authorizeExchange(
                        authorizeExchangeSpec -> {
                            authorizeExchangeSpec
                                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                            authorizeExchangeSpec.anyExchange().authenticated();
                        })
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityWebFilterChain clientAccessFilterChain(final ServerHttpSecurity http,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") final String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") final String jwkSetUri) {
        log.info("Initializing security with issuer URI {} and jwk set uri {}", issuerUri, jwkSetUri);

        // security config
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/**"))
                .authorizeExchange(
                        authorizeExchangeSpec -> {
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
                        })
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowedOrigins}") final List<String> allowedOrigins) {
        log.info("Initializing CORS with Origins {}", allowedOrigins);
        final CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.OPTIONS.name(), HttpMethod.POST.name()));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);

        return configSource;
    }

    /**
     * Returns the session timeout determined by Spring Boot.
     * <p>
     * Prior to Spring Boot 4, the timeout was resolved explicitly using {@link SessionProperties}
     * with
     * {@link ServerProperties#getServlet()} as a fallback,
     * mirroring the timeout resolution described in the
     * <a href="https://docs.spring.io/spring-boot/reference/web/spring-session.html">Spring
     * Session</a>
     * documentation.
     * <p>
     * Since Spring Boot 4, this resolution is encapsulated by {@link SessionTimeout}. An
     * {@link IllegalStateException} is thrown if no session timeout could be
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
