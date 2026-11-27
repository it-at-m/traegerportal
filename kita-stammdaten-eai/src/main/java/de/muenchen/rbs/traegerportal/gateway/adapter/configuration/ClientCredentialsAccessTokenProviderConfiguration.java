package de.muenchen.rbs.traegerportal.gateway.adapter.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.muenchen.rbs.traegerportal.gateway.adapter.ClientCredentialsAccessTokenProvider;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
@Profile("!no-security")
public class ClientCredentialsAccessTokenProviderConfiguration {

    @Bean
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public ClientCredentialsAccessTokenProvider clientCredentialsAccessTokenProvider(
            final WebClient.Builder webClientBuilder,
            @Value("${adapter.security.token-url}") final String tokenUrl,
            @Value("${adapter.security.client-id}") final String clientId,
            @Value("${adapter.security.client-secret}") final String clientSecret,
            @Value("${adapter.security.scope}") final String scope,
            @Value("${adapter.security.cache-expires-after-write}") final Duration cacheExpiresAfterWrite,
            @Value("${adapter.security.cache-maximum-size}") final int cacheMaximumSize

    ) {
        final WebClient webClient = webClientBuilder
                .baseUrl(tokenUrl)
                .build();

        final Cache<String, String> tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheExpiresAfterWrite)
                .maximumSize(cacheMaximumSize)
                .build();

        final ClientCredentialsAccessTokenProvider clientCredentialsAccessTokenProvider = new ClientCredentialsAccessTokenProvider(
                webClient,
                clientId,
                clientSecret,
                scope,
                tokenCache);

        log.info(
                "Initialized ClientCredentialsAccessTokenProvider [tokenUrl = {}, clientId = {}, scope = {}, cacheExpiresAfterWrite= {}, cacheMaximumSize={}]",
                tokenUrl,
                clientId,
                scope,
                cacheExpiresAfterWrite,
                cacheMaximumSize);

        return clientCredentialsAccessTokenProvider;
    }

}
