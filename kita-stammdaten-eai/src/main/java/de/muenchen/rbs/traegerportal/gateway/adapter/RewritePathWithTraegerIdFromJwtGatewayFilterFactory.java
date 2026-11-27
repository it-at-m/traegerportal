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

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;

/**
 * Gateway filter factory that resolves a traegerId from a configured JWT claim via the backend and
 * rewrites the request path using the resolved ID. Preserves the remaining path suffix, forwards
 * configured JWT claims as request headers with the "ORIGINAL_CLAIM" prefix and forwards the
 * original Authorization header as "ORIGINAL_HEADER_AUTHORIZATION".
 *
 * @author felix.haala
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RewritePathWithTraegerIdFromJwtGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RewritePathWithTraegerIdFromJwtGatewayFilterFactory.Config> {

    private final TraegerIdApiRestService traegerIdApiRestService;

    /**
     * Creates the gateway filter. Returns HTTP 401 for missing JWT authentication or required claims,
     * HTTP 404 if no traegerId is found, HTTP 502 if the backend call fails or HTTP 400 if the request
     * path does not match the configured prefix.
     *
     * @param config the filter configuration
     * @return @return the gateway filter that rewrites the request path and forwards the original
     *         claims and Authorization header
     * @throws IllegalArgumentException if the target path template does not contain exactly one
     *             placeholder
     */
    @Override
    @NullMarked
    public GatewayFilter apply(final Config config) {
        final UriTemplate targetTemplate = config.getTargetPathTemplateByJwtClaim();
        final List<String> variableNames = targetTemplate.getVariableNames();

        Assert.isTrue(variableNames.size() == 1,
                "targetPath must contain exactly one placeholder specifying the JWT claim to resolve into a traegerId");

        final String templateClaimName = variableNames.getFirst();

        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                // Only JWT-based authentication is supported
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                // Reject unauthenticated requests
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Missing JWT authentication")))
                .flatMap(jwt -> traegerIdApiRestService
                        .getTraegerIdByUnternehmenskontoId(
                                getRequiredClaim(jwt, templateClaimName))
                        .doOnError(ex -> log.warn(
                                "Failed to obtain traegerId from backend.", ex))
                        .onErrorMap(ex -> new ResponseStatusException(
                                HttpStatus.BAD_GATEWAY,
                                "Call to backend failed", ex))
                        .switchIfEmpty(Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "traegerId not found")))
                        .flatMap(traegerId -> {
                            final String targetPath = targetTemplate
                                    .expand(traegerId)
                                    .getRawPath();

                            final String newPath = replacePath(
                                    exchange.getRequest().getURI().getRawPath(),
                                    config.getPathPrefixToReplace(),
                                    targetPath);

                            final ServerHttpRequest request = exchange.getRequest()
                                    .mutate()
                                    .path(newPath)
                                    .headers(headers -> {
                                        config.getClaimsToForwardAsHeaders()
                                                .forEach(claimName -> headers.set(
                                                        "ORIGINAL_CLAIM_" + claimName.toUpperCase(),
                                                        getRequiredClaim(jwt, claimName)));
                                        headers.set(
                                                "ORIGINAL_HEADER_AUTHORIZATION",
                                                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                                    })
                                    .build();

                            return chain.filter(exchange.mutate()
                                    .request(request)
                                    .build());
                        }));
    }

    /**
     * Replaces the specified path prefix while preserving the remaining path suffix.
     *
     * @param originalPath the original request path
     * @param pathToReplace the path prefix to replace
     * @param targetPath the replacement path prefix
     * @return the rewritten request path
     * @throws ResponseStatusException with HTTP 400 if the path does not match the specified prefix
     */
    private String replacePath(
            final String originalPath,
            final String pathToReplace,
            final String targetPath) {

        // example: originalPath must equal /meintraeger or start with /meintraeger/
        if (!originalPath.equals(pathToReplace)
                && !originalPath.startsWith(pathToReplace + "/")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request path does not match the configured path prefix");
        }

        return targetPath + originalPath.substring(pathToReplace.length());
    }

    /**
     * Retrieves a required non-blank claim from the JWT.
     *
     * @param jwt the JWT containing the claim
     * @param claimName the name of the required claim
     * @return the claim value as a string
     * @throws ResponseStatusException with HTTP 401 if the claim is missing or blank
     */
    private String getRequiredClaim(
            final Jwt jwt,
            final String claimName) {
        final String value = jwt.getClaimAsString(claimName);

        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing claim: " + claimName);
        }

        return value;
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
         * JWT claims forwarded as request headers with the "ORIGINAL_CLAIM" prefix. Example:
         * ORIGINAL_CLAIM_PREFERRED_USERNAME
         */
        private final List<String> claimsToForwardAsHeaders;

        /**
         * Path prefix to replace. Example: /meintraeger
         */
        private final String pathPrefixToReplace;

        /**
         * Path template with exactly one JWT claim placeholder resolved to a traegerId. Example:
         * /traeger/{datenuebermittlerPseudonymId}
         */
        private final UriTemplate targetPathTemplateByJwtClaim;
    }
}
