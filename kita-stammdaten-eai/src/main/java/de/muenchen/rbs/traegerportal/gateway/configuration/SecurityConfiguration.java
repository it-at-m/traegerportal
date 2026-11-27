/*
 * The MIT License
 * Copyright © 2026 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Slf4j
@Configuration
@Profile("!no-security")
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final SessionTimeout sessionTimeout;

    @Bean
    @Order(1)
    public SecurityWebFilterChain webcomponentsFilterChain(final ServerHttpSecurity http) {
        // TODO: Discuss whether to use the "pub" prefix for publicly exposed endpoints.
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/webcomponents/**"))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        log.info("Initialized SecurityWebFilterChain for public webcomponents");
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain meinTraegerApiFilterChain(
            final ServerHttpSecurity http,
            final ResourceserverProperties resourceserverProperties,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") final String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") final String jwkSetUri) {

        final List<String> pathMatchers = resourceserverProperties
                .main()
                .securityMatcher()
                .pathMatchers();

        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        pathMatchers.toArray(String[]::new)))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()));

        log.info("Initialized SecurityWebFilterChain for MeinTraeger API [pathMatchers={}, issuerUri={}, jwkSetUri={}]", pathMatchers, issuerUri, jwkSetUri);
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityWebFilterChain clientAccessRulesFilterChain(
            final ServerHttpSecurity http,
            final ResourceserverProperties resourceserverProperties) {

        resourceserverProperties.clientAccessRules().forEach(clientEndpoint -> {
            final String pathMatchers = clientEndpoint.pathMatchers();
            final String clientId = clientEndpoint.clientId();
            final String issuerUri = clientEndpoint.jwt().issuerUri();
            final String jwkSetUri = clientEndpoint.jwt().jwkSetUri();

            final NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder
                    .withJwkSetUri(jwkSetUri)
                    .build();

            jwtDecoder.setJwtValidator(
                    JwtValidators.createDefaultWithIssuer(issuerUri));

            http
                    .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                            pathMatchers))
                    .authorizeExchange(exchanges -> exchanges
                            .anyExchange()
                            .access((authentication, _) -> authentication
                                    .map(auth -> checkClientAuthorization(auth, clientId, pathMatchers))))
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwt -> jwt.jwtDecoder(jwtDecoder)));

            log.info("Initialized client-specific SecurityWebFilterChain [pathMatchers={}, clientId={}, issuerUri={}, jwkSetUri={}]",
                    pathMatchers, clientId, issuerUri, jwkSetUri);
        });

        return http.build();
    }

    private static AuthorizationDecision checkClientAuthorization(
            final Authentication auth,
            final String allowedClientId,
            final String pathMatchers) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            final String clientId = jwtAuth.getToken().getClaimAsString("client_id");
            final boolean allowed = allowedClientId.equals(clientId);
            log.debug("Client authorization checked [clientId={}, pathMatchers={}, allowed={}]", clientId, pathMatchers, allowed);
            return new AuthorizationDecision(allowed);
        }
        log.debug("Client authorization denied: Unexpected authentication type [allowedClientId={}, pathMatchers={}]", allowedClientId, pathMatchers);
        return new AuthorizationDecision(false);
    }

    @Bean
    @Order(4)
    public SecurityWebFilterChain actuatorFilterChain(final ServerHttpSecurity http) {
        // TODO: Discuss
        //   "/api/*/actuator/info"
        //   "/actuator/info"
        //   "/actuator/metrics"
        //   "/actuator/sbom"
        //   "/actuator/sbom/application"
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                "/api/*/actuator/info",
                "/actuator/health",
                "/actuator/health/liveness",
                "/actuator/health/readiness",
                "/actuator/info",
                "/actuator/metrics",
                "/actuator/sbom",
                "/actuator/sbom/application"))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable);

        log.info("Initialized SecurityWebFilterChain for public Actuator endpoints");
        return http.build();
    }

    /**
     * Returns the session timeout determined by Spring Boot.
     * <p>
     * Prior to Spring Boot 4, the timeout was resolved explicitly using {@link SessionProperties} with
     * {@link ServerProperties#getServlet()} as a fallback,
     * mirroring the timeout resolution described in the
     * <a href="https://docs.spring.io/spring-boot/reference/web/spring-session.html">Spring
     * Session</a> documentation.
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
