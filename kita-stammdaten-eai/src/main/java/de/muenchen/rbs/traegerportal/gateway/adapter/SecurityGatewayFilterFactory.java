package de.muenchen.rbs.traegerportal.gateway.adapter;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<SecurityGatewayFilterFactory.Config> {

    private final ClientCredentialsAccessTokenProvider stammdatenAccessTokenProvider;

    public SecurityGatewayFilterFactory(ClientCredentialsAccessTokenProvider stammdatenAccessTokenProvider) {
        super(Config.class);
        this.stammdatenAccessTokenProvider = stammdatenAccessTokenProvider;
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()

                .mapNotNull(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))

                .flatMap(jwt -> stammdatenAccessTokenProvider.getAccessToken()
                        .flatMap(accessToken -> {

                            String ukId = jwt.getClaimAsString("datenuebermittlerPseudonymId");
                            String user = jwt.getClaimAsString("username");

                            String path = exchange.getRequest().getPath().value();
                            String pathReplacement = path.replaceFirst(
                                    "^/meintraeger", "/traeger/by-unternehmenskontoid/" + ukId);

                            URI targetUri = UriComponentsBuilder
                                    .fromUri(exchange.getRequest().getURI())
                                    .replacePath(pathReplacement)
                                    .build()
                                    .encode()
                                    .toUri();

                            ServerHttpRequest newRequest = exchange.getRequest()
                                    .mutate()
                                    .uri(targetUri)
                                    .headers(headers -> {
                                        headers.setBearerAuth(accessToken);
                                        headers.set(
                                                "Original-Authorization",
                                                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                                        headers.set(
                                                "Original-Username",
                                                user);
                                    })
                                    .build();

                            return chain.filter(
                                    exchange.mutate()
                                            .request(newRequest)
                                            .build());
                        }))

                .onErrorResume(ResponseStatusException.class, ex -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")));
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
