package de.muenchen.rbs.traegerportal.gateway.adapter;

import lombok.extern.slf4j.Slf4j;
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
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class StammdatenSecurityGatewayFilter2Factory extends AbstractGatewayFilterFactory<StammdatenSecurityGatewayFilter2Factory.Config> {

    private final ClientCredentialsAccessTokenProvider stammdatenAccessTokenProvider;
    private final TraegerIdApiRestService traegerIdApiRestService;

    public StammdatenSecurityGatewayFilter2Factory(final ClientCredentialsAccessTokenProvider stammdatenAccessTokenProvider,
            final TraegerIdApiRestService traegerIdApiRestService) {
        super(Config.class);
        this.stammdatenAccessTokenProvider = stammdatenAccessTokenProvider;
        this.traegerIdApiRestService = traegerIdApiRestService;
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                // Only JWT-based authentication is supported (expect valid token / request by Spring Security, expect client ValEAI by Spring Security)
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                // Reject unauthenticated requests
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing JWT authentication")))
                .flatMap(jwt -> {
                    Map<String, String> pathVariables = ServerWebExchangeUtils.getUriTemplateVariables(exchange);
                    //                    final String ukId = pathVariables.get("datenuebermittlerPseudonymId");
                    final String ukId = pathVariables.get("unternehmenskontoId");
                    final String user = jwt.getClaimAsString("preferred_username");

                    log.debug("Extracted datenuebermittlerPseudonymId: {}", ukId);
                    log.debug("Extracted preferred_username: {}", user);

                    // Reject requests with missing unternehmenskontoId
                    if (!StringUtils.hasText(ukId)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Missing unternehmenskontoId"));
                    }

                    return stammdatenAccessTokenProvider.getAccessToken()
                            .doOnError(ex -> log.warn("Failed to obtain access token for backend.", ex))
                            .flatMap(accessTokenForBackend -> {
                                return traegerIdApiRestService.getTraegerIdByUnternehmenskontoId(ukId)
                                        .doOnError(ex -> log.warn("Failed to obtain traeger id from backend.", ex))
                                        .flatMap(traegerId -> {
                                            final String path = exchange.getRequest().getPath().value();
                                            final String pathWithUkId = path.replace("(id)", traegerId.toString());

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
