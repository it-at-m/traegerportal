/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2026
 */
package de.muenchen.rbs.traegerportal.gateway.adapter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
@Component
public class TraegerIdApiRestService {

    private final WebClient webClient;
    private final ClientCredentialsAccessTokenProvider clientCredentialsAccessTokenProvider;
    private final String idPath;

    private static final int ID_CACHE_IN_SECONDS = 600;
    private final Cache<String, Long> idCache;

    /**
     * Creates a TraegerIdApiRestService
     *
     * @param webClientBuilder to initialize the RestClient
     * @param evUrl URL for making calls to Kita-Einrichtungsverwaltung
     */
    public TraegerIdApiRestService(final WebClient.Builder webClientBuilder, @Value("${adapter.einrichtungsverwaltung.base-url}") final String evUrl,
            @Value("${adapter.einrichtungsverwaltung.id-path}") final String idPath,
            final ClientCredentialsAccessTokenProvider tokenProvider) {
        this.webClient = webClientBuilder.baseUrl(evUrl).build();
        this.clientCredentialsAccessTokenProvider = tokenProvider;
        this.idPath = idPath;
        this.idCache = CacheBuilder.newBuilder().maximumSize(1)
                .expireAfterWrite(ID_CACHE_IN_SECONDS, TimeUnit.SECONDS)
                .build();
        log.info("Initialized with evUrl='{}' and idPath='{}'", evUrl, idPath);
    }

    /**
     * Returns the corresponding internal id given a unternehmenskonto id.
     * This is needed to ensure each traeger can only access his own data and we can query for it in
     * all related systems.
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
                                    log.error("Request for traeger id did not return 2XX successful status code, but returned status {}.",
                                            response.statusCode());
                                    return response.bodyToMono(String.class).flatMap(body -> Mono
                                            .error(new RuntimeException("Request for traeger id did not return 2XX successful status code. Body: " + body)));
                                }
                            });
                    return idResponse.flatMap(id -> {
                        log.debug("Aquired Id for unternehmenskonto {}.", unternehmenskontoId);
                        idCache.put(unternehmenskontoId, id);
                        return Mono.just(id);
                    });
                });
    }
}
