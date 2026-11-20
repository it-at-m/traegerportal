package de.muenchen.rbs.traegerportal.gateway.adapter.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.muenchen.rbs.traegerportal.gateway.adapter.ClientCredentialsAccessTokenProvider;
import de.muenchen.rbs.traegerportal.gateway.adapter.TraegerIdApiRestService;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class TraegerIdApiRestServiceConfiguration {

    @Bean
    public TraegerIdApiRestService traegerIdApiRestService(
            final WebClient.Builder webClientBuilder,
            @Value("${adapter.einrichtungsverwaltung.base-url}") final String evUrl,
            @Value("${adapter.einrichtungsverwaltung.id-path}") final String idPath,
            final ClientCredentialsAccessTokenProvider tokenProvider,
            @Value("${adapter.einrichtungsverwaltung.cache-expires-after-write}") final Duration cacheExpiresAfterWrite,
            @Value("${adapter.einrichtungsverwaltung.cache-maximum-size}") final int cacheMaximumSize) {
        final WebClient webClient = webClientBuilder
                .baseUrl(evUrl)
                .build();

        final Cache<String, Long> idCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheExpiresAfterWrite)
                .maximumSize(cacheMaximumSize)
                .build();

        final TraegerIdApiRestService traegerIdApiRestService = new TraegerIdApiRestService(
                webClient,
                idPath,
                idCache,
                tokenProvider);

        log.info(
                "Initialized TraegerIdApiRestService [evUrl = {}, idPath={}, cacheExpiresAfterWrite= {}, cacheMaximumSize={}]",
                evUrl,
                idPath,
                cacheExpiresAfterWrite,
                cacheMaximumSize);

        return traegerIdApiRestService;
    }

}
