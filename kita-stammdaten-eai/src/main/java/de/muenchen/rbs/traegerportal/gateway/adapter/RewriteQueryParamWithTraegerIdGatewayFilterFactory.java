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

import java.net.URI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Gateway filter factory that resolves a TraegerId from a configured query parameter containing a
 * pseudonym ID via the backend. Removes the source query parameter and sets the target query
 * parameter to the resolved TraegerId, preserving all other query parameters.
 *
 * @author felix.haala
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RewriteQueryParamWithTraegerIdGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RewriteQueryParamWithTraegerIdGatewayFilterFactory.Config> {

    private final TraegerIdApiRestService traegerIdApiRestService;

    /**
     * Creates the gateway filter. Returns HTTP 400 (Bad Request) if the source parameter is missing or
     * blank or HTTP 404 (Not Found) if no traegerId is found.
     *
     * @param config the filter configuration
     * @return the gateway filter that rewrites the query parameters
     */
    @Override
    @NullMarked
    public GatewayFilter apply(final Config config) {
        return (exchange, chain) -> {

            final ServerHttpRequest request = exchange.getRequest();
            final String pseudonymId = request.getQueryParams()
                    .getFirst(config.getSourceQueryParam());

            if (!StringUtils.hasText(pseudonymId)) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        config.getSourceQueryParam() + " missing"));
            }

            return traegerIdApiRestService
                    .getTraegerIdByUnternehmenskontoId(pseudonymId)
                    .switchIfEmpty(Mono.error(
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "traegerId not found")))
                    .flatMap(traegerId -> {
                        final URI targetUri = UriComponentsBuilder
                                .fromUri(request.getURI())
                                .replaceQueryParam(config.getSourceQueryParam())
                                .replaceQueryParam(config.getTargetQueryParam(), traegerId)
                                .build(true)
                                .toUri();

                        // update the request URI
                        final ServerHttpRequest modifiedRequest = request.mutate()
                                .uri(targetUri)
                                .build();

                        // continue the filter chain with the modified request
                        return chain.filter(exchange.mutate()
                                .request(modifiedRequest)
                                .build());
                    });
        };
    }

    @Override
    @NullMarked
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Config {
        /**
         * Query parameter containing the pseudonym ID to resolve and replace. Example:
         * datenuebermittlerPseudonymId
         */
        private final String sourceQueryParam;

        /**
         * Query parameter receiving the resolved traegerId. Example: traegerId
         */
        private final String targetQueryParam;
    }
}
