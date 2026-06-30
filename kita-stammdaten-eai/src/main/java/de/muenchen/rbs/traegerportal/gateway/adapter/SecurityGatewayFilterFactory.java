package de.muenchen.rbs.traegerportal.gateway.adapter;

import java.net.URI;
import java.nio.charset.Charset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<SecurityGatewayFilterFactory.Config> {

    @Autowired
    private ClientCredentialsAccessTokenProvider stammdatenAccesTokenProvider;

    @Autowired
    private ReactiveJwtDecoder jwtDecoder;

    public SecurityGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            final String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                final String jwtToken = authorizationHeader.substring(7);

                return jwtDecoder.decode(jwtToken).flatMap(jwt -> {
                    final String ukId = jwt.getClaimAsString("datenuebermittlerPseudonymId");
                    final String user = jwt.getClaimAsString("username");

                    final URI newUri = UriComponentsBuilder
                            .fromUri(exchange.getRequest().getURI())
                            .queryParam("datenuebermittlerPesudonymId", ukId)
                            .queryParam("username", user)
                            .build()
                            .toUri();

                    return stammdatenAccesTokenProvider.getAccessToken().flatMap(accessToken -> {
                        final ServerHttpRequest newRequest = exchange.getRequest().mutate()
                                .uri(newUri)
                                .headers(httpHeaders -> {
                                    httpHeaders.remove("Authorization");
                                    httpHeaders.set("Authorization", "Bearer " + accessToken);
                                    httpHeaders.set("UserAuthorization", authorizationHeader);
                                }).build();
                        return chain.filter(exchange.mutate().request(newRequest).build());
                    });
                }).onErrorResume(e -> {
                    // Handle JWT decode errors
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().set("Content-Type", "application/json");
                    return exchange.getResponse().writeWith(
                            Mono.just(exchange.getResponse().bufferFactory().wrap(
                                    "{\"error\": \"Invalid token\"}".getBytes(Charset.defaultCharset()))));
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
