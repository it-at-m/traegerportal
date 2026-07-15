package de.muenchen.rbs.traegerportal.gateway;

import de.muenchen.rbs.traegerportal.gateway.adapter.ClientCredentialsAccessTokenProvider;
import de.muenchen.rbs.traegerportal.gateway.adapter.SecurityGatewayFilterFactory;
import de.muenchen.rbs.traegerportal.gateway.configuration.GatewayConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * Test configuration providing mocked authentication and JWT infrastructure.
 */
@TestConfiguration
@Import({ GatewayConfig.class, SecurityGatewayFilterFactory.class })
public class OAuthSecurityMockConfiguration {

    public static final String JWT_KEYS_HEADERS_ALG = "alg";
    public static final String JWT_KEYS_CLAIMS_DATENUEBERMITTLERPSEUDONYMID = "datenuebermittlerPseudonymId";
    public static final String JWT_KEYS_CLAIMS_USERNAME = "username";

    public static final String JWT_FRONTEND_DEFAULT_TOKEN_VALUE = "test-token";
    public static final Map<String, String> JWT_FRONTEND_DEFAULT_HEADERS = Map.of(
            JWT_KEYS_HEADERS_ALG, "none");
    public static final Map<String, String> JWT_FRONTEND_DEFAULT_CLAIMS = Map.of(
            JWT_KEYS_CLAIMS_DATENUEBERMITTLERPSEUDONYMID, "32443223",
            JWT_KEYS_CLAIMS_USERNAME, "max.mustermann");

    public static final String JWT_BACKEND_DEFAULT_TOKEN_VALUE = "service-token";
    public static final Map<String, String> JWT_BACKEND_DEFAULT_HEADERS = Map.of(
            "notYetSpecified", "notYetSpecified");
    public static final Map<String, String> JWT_BACKEND_DEFAULT_CLAIMS = Map.of(
            "notYetSpecified", "notYetSpecified");

    @Setter
    @Getter
    private Jwt jwtFrontend = Jwt.withTokenValue(JWT_FRONTEND_DEFAULT_TOKEN_VALUE)
            .headers(headers -> headers.putAll(JWT_FRONTEND_DEFAULT_HEADERS))
            .claims(claims -> claims.putAll(JWT_FRONTEND_DEFAULT_CLAIMS))
            .build();

    @Getter
    @Setter
    private Jwt jwtBackend = Jwt.withTokenValue(JWT_BACKEND_DEFAULT_TOKEN_VALUE)
            .headers(headers -> headers.putAll(JWT_BACKEND_DEFAULT_HEADERS))
            .claims(claims -> claims.putAll(JWT_BACKEND_DEFAULT_CLAIMS))
            .build();

    public void addFrontendBearerAuth(final HttpHeaders headers) {
        headers.setBearerAuth(this.getJwtFrontend().getTokenValue());
    }

    public void addBackendBearerAuth(final HttpHeaders headers) {
        headers.setBearerAuth(this.getJwtBackend().getTokenValue());
    }

    @Bean
    @Primary
    public ReactiveJwtDecoder mJwtDecoder() {
        final ReactiveJwtDecoder mock = mock(ReactiveJwtDecoder.class);

        when(mock.decode(JWT_FRONTEND_DEFAULT_TOKEN_VALUE))
                .thenAnswer(invocation -> jwtFrontend != null
                        ? Mono.just(jwtFrontend)
                        : Mono.error(new IllegalStateException("Frontend JWT not available")));

        when(mock.decode(JWT_BACKEND_DEFAULT_TOKEN_VALUE))
                .thenAnswer(invocation -> jwtBackend != null
                        ? Mono.just(jwtBackend)
                        : Mono.error(new IllegalStateException("Backend JWT not available")));

        return mock;
    }

    @Bean
    @Primary
    public ClientCredentialsAccessTokenProvider mClientCredentialsAccessTokenProvider() {
        final ClientCredentialsAccessTokenProvider mock = mock(ClientCredentialsAccessTokenProvider.class);

        when(mock.getAccessToken()).thenAnswer(invocation -> {
            if (getJwtBackend() == null) {
                return Mono.error(new IllegalStateException("Access token not available"));
            }
            return Mono.just(getJwtBackend().toString());
        });

        return mock;
    }

    @Bean
    @Primary
    public WebTestClient webTestClientWithMockedSecurity(ApplicationContext context) {
        return WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

}
