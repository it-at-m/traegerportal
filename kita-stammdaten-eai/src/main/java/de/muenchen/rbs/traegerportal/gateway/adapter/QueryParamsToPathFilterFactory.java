package de.muenchen.rbs.traegerportal.gateway.adapter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class QueryParamsToPathFilterFactory extends AbstractGatewayFilterFactory<QueryParamsToPathFilterFactory.Config> {

    public QueryParamsToPathFilterFactory() {
        super(Config.class);
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            UriTemplate template = new UriTemplate(exchange.getRequest().getPath().toString());

            List<String> usedParams = template.getVariableNames();

            ServerHttpRequest request = exchange.getRequest();
            MultiValueMap<String, String> queryParams = request.getQueryParams();

            // no need to check for multiple values per key, as GlobalRequestParameterPollutionFilter prevents multiple occurrences
            Map<String, String> variables = queryParams.toSingleValueMap();

            // check for missing required parameters
            List<String> missingParams = usedParams.stream()
                    .filter(p -> !variables.containsKey(p))
                    .toList();

            if (!missingParams.isEmpty()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Missing parameters [parameters = %s]".formatted(missingParams)));
            }

            // set path variables from query parameters
            String newPath = template.expand(variables).getRawPath();

            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUri(request.getURI())
                    .replacePath(newPath);

            // remove only the used query parameters
            usedParams.forEach(uriBuilder::replaceQueryParam);

            URI newUri = uriBuilder.build(true).toUri();

            ServerHttpRequest newRequest = request.mutate()
                    .uri(newUri)
                    .build();

            // continue filter chain with the modified request
            return chain.filter(
                    exchange.mutate()
                            .request(newRequest)
                            .build()
            );
        };
    }

    public static Config builder() {
        return new Config();
    }

    @Override
    @NullMarked
    public Class<Config> getConfigClass() {

        return Config.class;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Config {
        // Add any configuration properties if needed

        private final Map<String, String> mappings = new LinkedHashMap<>();

        public Config map(String key, String value) {
            mappings.put(key, value);
            return this;
        }
    }
}
