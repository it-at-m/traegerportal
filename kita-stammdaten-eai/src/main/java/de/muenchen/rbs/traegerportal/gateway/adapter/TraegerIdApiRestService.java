/*

 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2022
 */
package de.muenchen.rbs.traegerportal.gateway.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
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

    /**
     * Erstellt einen ClientCredentialsAccessTokenProvider
     *
     * @param webClientBuilder zum Initialisieren des WebClient
     * @param evUrl URL der Einrichtungsverwaltung
     */
    public TraegerIdApiRestService(final WebClient.Builder webClientBuilder, @Value("${adapter.einrichtungsverwaltung.base-url}") final String evUrl,
            final ClientCredentialsAccessTokenProvider tokenProvider) {
        this.webClient = webClientBuilder.baseUrl(evUrl).build();
        this.clientCredentialsAccessTokenProvider = tokenProvider;
        log.info("Initialized with evUrl='{}'", evUrl);
    }

    /**
     * @param unternehmenskontoId unternehmenskontoId to query for
     * @return retrieves an id for a traeger given its unternehmenskontoid
     */
    public final Mono<Long> getTraegerIdByUnternehmenskontoId(final String unternehmenskontoId) {
        final Mono<Long> responseBody = this.clientCredentialsAccessTokenProvider.getAccessToken().flatMap(accessToken -> {
            return this.webClient.get()
                    .uri("/external/traeger/by-unternehmenskontoid/" + unternehmenskontoId + "/id")
                    .header("Authorization", "Bearer " + accessToken)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(Long.class);
                        } else {
                            log.warn("Request for traeger id did not return 2XX successful status code, but returned status {}.", response.statusCode());
                            throw new RuntimeException("Request for traeger id did not return 2XX successful status code.");
                        }
                    });
        });

        return responseBody;
    }
}
