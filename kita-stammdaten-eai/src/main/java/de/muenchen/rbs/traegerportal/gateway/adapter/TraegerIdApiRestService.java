/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2026
 */
package de.muenchen.rbs.traegerportal.gateway.adapter;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * Resolves Traeger IDs from Unternehmenskonto Ids.
 *
 * @author m.zollbrecht
 * @see <a href=
 *      "https://auth0.com/docs/flows/client-credentials-flow">https://auth0.com/docs/flows/client-credentials-flow</a>
 */
@Slf4j
@RequiredArgsConstructor
public class TraegerIdApiRestService {

    private final WebClient webClient;
    private final String idPath;
    private final Cache<String, Long> idCache;
    private final ClientCredentialsAccessTokenProvider clientCredentialsAccessTokenProvider;

    /**
     * Returns the corresponding internal id given a unternehmenskonto id. This is needed to ensure each
     * traeger can only access his own data and we can query
     * for it in all related systems.
     *
     * @param unternehmenskontoId unternehmenskontoId to query for
     * @return retrieves an id for a traeger given its unternehmenskonto id
     */
    public final Mono<Long> getTraegerIdByUnternehmenskontoId(final String unternehmenskontoId) {
        final Long idFromCache = idCache.getIfPresent(unternehmenskontoId);
        if (idFromCache != null) {
            log.debug("Id found in cache.");
            return Mono.just(idFromCache);
        }

        log.debug("Requesting id for unternehmenskonto from ke+ {}...", unternehmenskontoId);
        return this.clientCredentialsAccessTokenProvider.getAccessToken()
                .flatMap(accessToken -> {
                    final Mono<Long> idResponse = this.webClient.get()
                            .uri(idPath, unternehmenskontoId)
                            .header("Authorization", "Bearer " + accessToken)
                            .exchangeToMono(response -> {
                                if (response.statusCode().is2xxSuccessful()) {
                                    return response.bodyToMono(Long.class);
                                } else {
                                    log.error("Request for traeger id ({}) did not return 2XX successful status code, but returned status {}.",
                                            response.request().getURI().toString(),
                                            response.statusCode());
                                    return response.bodyToMono(String.class).flatMap(body -> Mono
                                            .error(new RuntimeException("Request for traeger id did not return 2XX successful status code. Body: " + body)));
                                }
                            });
                    return idResponse.flatMap(id -> {
                        log.debug("Aquired Id {} for unternehmenskonto {}.", id, unternehmenskontoId);
                        idCache.put(unternehmenskontoId, id);
                        return Mono.just(id);
                    });
                });
    }
}
