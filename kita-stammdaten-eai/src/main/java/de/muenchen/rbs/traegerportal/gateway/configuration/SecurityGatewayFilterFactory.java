package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import de.muenchen.rbs.traegerportal.gateway.adapter.ClientCredentialsAccessTokenProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<SecurityGatewayFilterFactory.Config> {

    @Autowired
    private ClientCredentialsAccessTokenProvider stammdatenAccesTokenProvider;

    public SecurityGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwtToken = authorizationHeader.substring(7);
                log.info("Token received: {}", jwtToken);

                URI newUri = UriComponentsBuilder
                        .fromUri(exchange.getRequest().getURI())
                        .build()
                        .toUri();

                return stammdatenAccesTokenProvider.getAccessToken().flatMap(accessToken -> {
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                            .uri(newUri)
                            .headers(httpHeaders -> {
                                httpHeaders.remove("Authorization");
                                httpHeaders.set("Authorization", "Bearer " + accessToken);
                                httpHeaders.set("UserAuthorization", authorizationHeader);
                            }).build();
                    return chain.filter(exchange.mutate().request(newRequest).build());
                });
            } else {
                log.debug("No Authorization found. Short-circuititing to 401 response.");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().set("Content-Type", "application/json");
                exchange.getResponse().setComplete();

                return chain.filter(exchange);
            }

        };
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    public static class Config {
        // Add any configuration properties if needed
    }
}