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
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

/**
 * Gateway filter factory that Converts query parameters to URL path variables and removes them from
 * the request.
 *
 * @author felix.haala
 */
@Slf4j
@Component
public class RewriteQueryParamsToPathGatewayFilterFactory extends AbstractGatewayFilterFactory<RewriteQueryParamsToPathGatewayFilterFactory.Config> {

    /**
     * Creates the gateway filter.
     *
     * @param config the filter configuration
     * @return the gateway filter that rewrites the request URI
     * @throws IllegalArgumentException if the target path is missing or not absolute
     */
    @Override
    @NullMarked
    public GatewayFilter apply(final Config config) {
        if (config.getTargetPath() == null || !config.getTargetPath().toString().startsWith("/")) {
            throw new IllegalArgumentException("targetPath must be an absolute path");
        }

        final UriTemplate pathTemplate = config.getTargetPath();

        return (exchange, chain) -> filterRequest(exchange, chain, pathTemplate);
    }

    /**
     * Expands the target path template using query parameter values and removes the consumed parameters
     * while preserving all remaining query parameters. Returns HTTP 400 (Bad Request) if any required
     * query parameter is missing.
     *
     * @param exchange the current server web exchange
     * @param chain the gateway filter chain
     * @param pathTemplate the target path template
     * @return a Mono that completes when the filter chain processing is finished
     */
    private Mono<Void> filterRequest(
            final ServerWebExchange exchange,
            final GatewayFilterChain chain,
            final UriTemplate pathTemplate) {

        final ServerHttpRequest request = exchange.getRequest();

        final List<String> requiredParams = pathTemplate.getVariableNames();

        // multiple values per key are already prevented by GlobalRequestParameterPollutionFilter
        final Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();

        // check for missing required parameters
        final List<String> missingParams = requiredParams.stream()
                .filter(p -> !queryParams.containsKey(p))
                .toList();

        if (!missingParams.isEmpty()) {
            final String message = "Missing parameters [parameters = %s]".formatted(missingParams);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, message));
        }

        // replace path variables with query parameter values
        final String newPath = pathTemplate.expand(queryParams).getRawPath();

        final UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUri(request.getURI())
                .replacePath(newPath);

        // remove only the query parameters that were used in the path
        requiredParams.forEach(uriBuilder::replaceQueryParam);

        final URI newUri = uriBuilder.build(true).toUri();

        // update the request URI
        final ServerHttpRequest modifiedRequest = request.mutate()
                .uri(newUri)
                .build();

        // continue the filter chain with the modified request
        return chain.filter(exchange.mutate()
                .request(modifiedRequest)
                .build());
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
         * Absolute path template with placeholders resolved from query parameters. Used query parameters
         * are removed from the request. Example: /traeger/{traegerId}/vorgaenge/{vorgangId}
         */
        private final UriTemplate targetPath;
    }
}
