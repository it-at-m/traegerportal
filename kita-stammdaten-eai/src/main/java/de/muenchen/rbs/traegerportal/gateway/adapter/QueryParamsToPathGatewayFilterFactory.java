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
 * Converts query parameters into URL path variables.
 *
 * @author felix.haala
 */
@Slf4j
@Component
public class QueryParamsToPathGatewayFilterFactory extends AbstractGatewayFilterFactory<QueryParamsToPathGatewayFilterFactory.Config> {

    @Override
    @NullMarked
    public GatewayFilter apply(final Config config) {
        if (config.getTargetPath() == null || !config.getTargetPath().startsWith("/")) {
            throw new IllegalArgumentException("targetPath must be an absolute path");
        }

        final UriTemplate pathTemplate = new UriTemplate(config.getTargetPath());

        return (exchange, chain) -> filterRequest(exchange, chain, pathTemplate);
    }

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

        // create a new request with the updated URI
        final ServerHttpRequest newRequest = request.mutate()
                .uri(newUri)
                .build();

        final ServerWebExchange newExchange = exchange.mutate()
                .request(newRequest)
                .build();

        // continue the filter chain with the modified request
        return chain.filter(newExchange);
    }

    @Override
    @NullMarked
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Config {
        private final String targetPath;
    }
}
