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
public class TraegerIdApiRestServiceTest {

    private ClientCredentialsAccessTokenProvider tokenProvider;
    private WebClient webClient;

    private TraegerIdApiRestService sut;

    private final Long testTraegerId = 123L;
    private final String testToken = "testToken";
    private final String testEvUrl = "testEvUrl";

    @BeforeEach
    void setUp() {
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        tokenProvider = mock(ClientCredentialsAccessTokenProvider.class);

        webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.exchangeToMono(any())).thenReturn(Mono.just(testTraegerId));

        when(webClientBuilder.baseUrl(Mockito.any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(tokenProvider.getAccessToken()).thenReturn(Mono.just(testToken));

        sut = new TraegerIdApiRestService(webClientBuilder, testEvUrl, tokenProvider);
    }

    @Test
    public void testIdResolution_ok() {
        String ukId = "test-uk-id";
        Long result = sut.getTraegerIdByUnternehmenskontoId(ukId).block();

        assertThat(result).isEqualTo(testTraegerId);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(testToken));
        Mockito.verify(webClient.get(), times(1)).uri("/external/traeger/by-unternehmenskontoid/" + ukId + "/id");
    }

    @Test
    public void testIdResolution_uses_cache() {
        String ukId = "test-uk-id";
        Long result = sut.getTraegerIdByUnternehmenskontoId(ukId).block();

        assertThat(result).isEqualTo(testTraegerId);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(testToken));
        Mockito.verify(webClient.get(), times(1)).uri("/external/traeger/by-unternehmenskontoid/" + ukId + "/id");
        
        result = sut.getTraegerIdByUnternehmenskontoId(ukId).block();

        // still 1 of each call after second service call
        assertThat(result).isEqualTo(testTraegerId);
        Mockito.verify(tokenProvider, times(1)).getAccessToken();
        Mockito.verify(webClient.get().uri(""), times(1)).header(Mockito.eq("Authorization"), Mockito.contains(testToken));
        Mockito.verify(webClient.get(), times(1)).uri("/external/traeger/by-unternehmenskontoid/" + ukId + "/id");
    }
}
