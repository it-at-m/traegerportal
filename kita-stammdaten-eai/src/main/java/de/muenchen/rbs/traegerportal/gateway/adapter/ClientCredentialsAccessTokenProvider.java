/*

 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2022
 */
package de.muenchen.rbs.traegerportal.gateway.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ClientCredentialsAccessTokenProvider {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final Cache<String, String> tokenCache;

    /**
     * @return retrieves an access token using client credentials flow
     */
    @SuppressWarnings("PMD.LooseCoupling")
    public final Mono<String> getAccessToken() {
        final String token = tokenCache.getIfPresent(this.clientId);
        if (token != null) {
            log.debug("Token found in cache.");
            return Mono.just(token);
        }

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", this.clientId);
        params.add("client_secret", this.clientSecret);
        params.add("scope", this.scope);

        log.debug("Requesting new access token...");
        final Mono<HashMap<String, String>> responseBody = this.webClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(new ParameterizedTypeReference<>() {
                        });
                    } else {
                        log.error("Request for access token did not return 2XX successful status code, but returned status {}.", response.statusCode());
                        throw new RuntimeException(
                                "Request for aquiring access token did not return 2XX successful status code.");
                    }
                });

        return responseBody.flatMap(response -> {
            final String accessToken = response.get("access_token");
            final int expiresInSeconds = Integer.parseInt(response.get("expires_in"));
            log.debug("Aquired access token (expires in: {} s)", expiresInSeconds);

            final long expiresAfterWrite = tokenCache.policy()
                    .expireAfterWrite()
                    .orElseThrow()
                    .getExpiresAfter(TimeUnit.SECONDS);

            if (expiresInSeconds <= expiresAfterWrite) {
                log.error(
                        "New access token expires in {} seconds, but it will be cached and used for the next {} seconds!" +
                                "Consider configuring a shorter token cache duration!",
                        expiresInSeconds, expiresAfterWrite);
            }
            tokenCache.put(this.clientId, accessToken);
            return Mono.just(accessToken);
        });
    }
}
