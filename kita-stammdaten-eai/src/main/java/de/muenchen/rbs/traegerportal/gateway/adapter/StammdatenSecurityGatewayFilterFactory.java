package de.muenchen.rbs.traegerportal.gateway.adapter;

import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StammdatenSecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<StammdatenSecurityGatewayFilterFactory.Config> {

    private final ClientCredentialsAccessTokenProvider stammdatenAccessTokenProvider;

    public StammdatenSecurityGatewayFilterFactory(final ClientCredentialsAccessTokenProvider stammdatenAccessTokenProvider) {
        super(Config.class);
        this.stammdatenAccessTokenProvider = stammdatenAccessTokenProvider;
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()

                // Only JWT-based authentication is supported
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                // Reject unauthenticated requests
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing JWT authentication")))

                .flatMap(jwt -> {
                    final String ukId = jwt.getClaimAsString("datenuebermittlerPseudonymId");
                    final String user = jwt.getClaimAsString("preferred_username");

                    // Reject requests with missing claims
                    if (!StringUtils.hasText(ukId) || !StringUtils.hasText(user)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing claim"));
                    }

                    final String path = exchange.getRequest().getPath().value();
                    final String pathWithUkId = path.replace("(ukId)", ukId);

                    return stammdatenAccessTokenProvider.getAccessToken()
                            .doOnError(ex -> log.warn("Failed to obtain access token for backend.", ex))

                            .flatMap(accessTokenForBackend -> {
                                final ServerHttpRequest requestToBackend = exchange.getRequest()
                                        .mutate()
                                        .path(pathWithUkId)
                                        .headers(headers -> {
                                            headers.setBearerAuth(accessTokenForBackend);
                                            headers.set(
                                                    "Original-Authorization",
                                                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                                            headers.set("Original-Username", user);
                                        })
                                        .build();
                                        
                                return chain.filter(
                                        exchange.mutate()
                                                .request(requestToBackend)
                                                .build());
                            })
                            .onErrorMap(ex -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Call to backend failed", ex));
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
