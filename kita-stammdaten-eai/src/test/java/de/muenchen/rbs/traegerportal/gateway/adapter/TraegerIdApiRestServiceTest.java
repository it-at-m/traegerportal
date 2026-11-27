/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2026
 */
package de.muenchen.rbs.traegerportal.gateway.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import de.muenchen.rbs.traegerportal.gateway.adapter.configuration.TraegerIdApiRestServiceConfiguration;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
class TraegerIdApiRestServiceTest {

    private ClientCredentialsAccessTokenProvider tokenProvider;
    private WebClient webClient;

    private TraegerIdApiRestService sut;

    private static final String TEST_UK_ID = "test-uk-id";
    private static final Long TEST_TRAEGER_ID = 123L;
    private static final String TEST_TOKEN = "testToken";
    private static final String EV_ID_PATH = "/external/traeger/by-unternehmenskontoid/{ukId}/id";

    @BeforeEach
    void setUp() {
        tokenProvider = mock(ClientCredentialsAccessTokenProvider.class);

        webClient = mock(WebClient.class);
        final WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.just(TEST_TRAEGER_ID));

        final Cache<String, Long> cache = TraegerIdApiRestServiceConfiguration.getCache(Duration.ofSeconds(600), 1000);

        when(tokenProvider.getAccessToken()).thenReturn(Mono.just(TEST_TOKEN));

        sut = new TraegerIdApiRestService(webClient, EV_ID_PATH, cache, tokenProvider);
    }

    @Test
    void testIdResolution_ok() {
        final Long result = sut.getTraegerIdByUnternehmenskontoId(TEST_UK_ID).block();

        assertThat(result).isEqualTo(TEST_TRAEGER_ID);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(TEST_TOKEN));
        Mockito.verify(webClient.get(), times(1)).uri(EV_ID_PATH, TEST_UK_ID);
    }

    @Test
    void testIdResolution_uses_cache() {
        Long result = sut.getTraegerIdByUnternehmenskontoId(TEST_UK_ID).block();

        assertThat(result).isEqualTo(TEST_TRAEGER_ID);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(TEST_TOKEN));
        Mockito.verify(webClient.get(), times(1)).uri(EV_ID_PATH, TEST_UK_ID);

        result = sut.getTraegerIdByUnternehmenskontoId(TEST_UK_ID).block();

        // still 1 of each call after second service call
        assertThat(result).isEqualTo(TEST_TRAEGER_ID);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(TEST_TOKEN));
        Mockito.verify(webClient.get(), times(1)).uri(EV_ID_PATH, TEST_UK_ID);
    }
}
