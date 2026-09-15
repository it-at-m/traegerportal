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

    private static final Long TEST_TRAEGER_ID = 123L;
    private static final String TEST_TOKEN = "testToken";
    private static final String TEST_EV_URL = "testEvUrl";

    @BeforeEach
    void setUp() {
        final WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        tokenProvider = mock(ClientCredentialsAccessTokenProvider.class);

        webClient = mock(WebClient.class);
        final WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        final WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.just(TEST_TRAEGER_ID));
        
        when(webClientBuilder.baseUrl(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        when(tokenProvider.getAccessToken()).thenReturn(Mono.just(TEST_TOKEN));

        sut = new TraegerIdApiRestService(webClientBuilder, TEST_EV_URL, "/external/traeger/by-unternehmenskontoid/{ukId}/id", tokenProvider);
    }

    @Test
    void testIdResolution_ok() {
        final String ukId = "test-uk-id";
        final Long result = sut.getTraegerIdByUnternehmenskontoId(ukId).block();

        assertThat(result).isEqualTo(TEST_TRAEGER_ID);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(TEST_TOKEN));
        Mockito.verify(webClient.get(), times(1)).uri("/external/traeger/by-unternehmenskontoid/{ukId}/id", ukId);
    }

    @Test
    void testIdResolution_uses_cache() {
        final String ukId = "test-uk-id";
        Long result = sut.getTraegerIdByUnternehmenskontoId(ukId).block();

        assertThat(result).isEqualTo(TEST_TRAEGER_ID);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(TEST_TOKEN));
        Mockito.verify(webClient.get(), times(1)).uri("/external/traeger/by-unternehmenskontoid/{ukId}/id", ukId);

        result = sut.getTraegerIdByUnternehmenskontoId(ukId).block();

        // still 1 of each call after second service call
        assertThat(result).isEqualTo(TEST_TRAEGER_ID);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(TEST_TOKEN));
        Mockito.verify(webClient.get(), times(1)).uri("/external/traeger/by-unternehmenskontoid/{ukId}/id", ukId);
    }
}
