package de.muenchen.rbs.traegerportal.gateway.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Profile("!no-security")
public class EinrichtungsverwaltungAdapterConfiguration {

    @Bean
    public ClientCredentialsAccessTokenProvider tokenProvider(WebClient.Builder webClientBuilder,
            @Value("${adapter.einrichtungsverwaltung.security.token-url}") final String tokenUrl,
            @Value("${adapter.einrichtungsverwaltung.security.client-id}") final String clientId,
            @Value("${adapter.einrichtungsverwaltung.security.client-secret}") final String clientSecret,
            @Value("${adapter.einrichtungsverwaltung.security.scope}") final String scope,
            @Value("${adapter.einrichtungsverwaltung.security.cache-seconds}") final int tokenCacheInSeconds) {
        return new ClientCredentialsAccessTokenProvider(webClientBuilder, tokenUrl, clientId, clientSecret, scope, tokenCacheInSeconds);
    }
}
