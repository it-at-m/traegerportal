/*

 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2022
 */
package de.muenchen.rbs.traegerportal.gateway.adapter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * Provides an Access Token, which is requested via Client Credentials Flow.
 *
 * @author m.zollbrecht
 * @see <a href=
 *      "https://auth0.com/docs/flows/client-credentials-flow">https://auth0.com/docs/flows/client-credentials-flow</a>
 */
@Slf4j
public class ClientCredentialsAccessTokenProvider {

    private final WebClient webClient;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final int tokenCacheInSeconds;
    private final Cache<String, String> tokenCache;

    /**
     * Erstellt einen ClientCredentialsAccessTokenProvider
     *
     * @param webClientBuilder zum Initialisieren des WebClient
     * @param tokenUrl URL, an der Auth-Tokens abgeholt werden sollen
     * @param clientId client-id mit der Auth-Tokens abgeholt werden sollen
     * @param clientSecret client-secret, mit dem Auth-Tokens abgeholt werden sollen
     * @param scope scope für die Anmeldung
     * @param tokenCacheInSeconds Aufbewahrungszeit für Tokens
     */
    public ClientCredentialsAccessTokenProvider(final WebClient.Builder webClientBuilder, final String tokenUrl, final String clientId,
            final String clientSecret, final String scope, final int tokenCacheInSeconds) {
        this.webClient = webClientBuilder.baseUrl(tokenUrl).build();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.tokenCacheInSeconds = tokenCacheInSeconds;
        log.info("Initialized with tokenUrl='{}', client-id: '{}' and scope '{}'.", this.tokenUrl, this.clientId,
                this.scope);

        tokenCache = CacheBuilder.newBuilder().maximumSize(1)
                .expireAfterWrite(this.tokenCacheInSeconds, TimeUnit.SECONDS)
                .build();

        this.getAccessToken();

        log.info("Retrieved initial access token, caching for {} seconds...", this.tokenCacheInSeconds);
    }

    /**
     * @return retrieves an access token using client credentials flow
     */
    public final Mono<String> getAccessToken() {
        final String token = tokenCache.getIfPresent(this.clientId);
        if (token != null) {
            return Mono.just(token);
        }

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", this.clientId);
        params.add("client_secret", this.clientSecret);
        params.add("scope", this.scope);

        final Mono<HashMap<String, String>> responseBody = this.webClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(new ParameterizedTypeReference<>() {
                        });
                    } else {
                        throw new RuntimeException(
                                "Request for aquiring access token did not return 2XX successful status code.");
                    }
                });

        return responseBody.flatMap(response -> {
            final String accessToken = response.get("access_token");
            final int expiresInSeconds = Integer.parseInt(response.get("expires_in"));
            log.info("Aquired access token (expires in: {} s)", expiresInSeconds);
            if (expiresInSeconds <= this.tokenCacheInSeconds) {
                log.error(
                        "New access token expires in {} seconds, but it will be cached and used for the next {} seconds!" +
                                "Consider configuring a shorter token cache duration!",
                        expiresInSeconds, this.tokenCacheInSeconds);
            }
            tokenCache.put(this.clientId, accessToken);
            return Mono.just(accessToken);
        }).switchIfEmpty(Mono.just("default"));
    }
}
