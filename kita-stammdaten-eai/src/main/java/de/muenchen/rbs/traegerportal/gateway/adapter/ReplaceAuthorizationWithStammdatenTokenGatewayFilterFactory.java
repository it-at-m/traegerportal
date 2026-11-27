/*
 * The MIT License
 * Copyright © 2026 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.rbs.traegerportal.gateway.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Gateway filter factory that replaces the incoming Authorization header with a Bearer token
 * obtained via the client credentials flow for the Stammdaten backend.
 *
 * @author felix.haala
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplaceAuthorizationWithStammdatenTokenGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ReplaceAuthorizationWithStammdatenTokenGatewayFilterFactory.Config> {

    private final ClientCredentialsAccessTokenProvider stammdatenAccessTokenProvider;

    /**
     * Creates the gateway filter. Returns HTTP 502 (Bad Gateway) if token retrieval fails or yields no
     * token.
     *
     * @param config the filter configuration
     * @return the gateway filter that modifies the Authorization header
     */
    @Override
    @NullMarked
    public GatewayFilter apply(final Config config) {
        return (exchange, chain) -> stammdatenAccessTokenProvider.getAccessToken()
                .doOnError(ex -> log.warn(
                        "Failed to obtain access token for backend.", ex))
                .onErrorMap(ex -> new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Failed to obtain access token for backend", ex))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Missing access token for backend")))
                .flatMap(accessToken -> {
                    final ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .headers(headers -> headers.setBearerAuth(accessToken))
                            .build();

                    return chain.filter(exchange.mutate()
                            .request(request)
                            .build());
                });
    }

    @Override
    @NullMarked
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    public static class Config {
        // Add any configuration properties if needed
    }
}
