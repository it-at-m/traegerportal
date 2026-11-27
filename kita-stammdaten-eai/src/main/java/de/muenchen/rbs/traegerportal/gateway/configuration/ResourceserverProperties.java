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
package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the resource server, defining default security settings and
 * client-specific access rules.
 *
 * @param main the default security configuration
 * @param clientAccessRules the list of client-specific access rules
 * @author felix.haala
 */
@ConfigurationProperties("resourceserver")
public record ResourceserverProperties(
        Main main,
        List<ClientAccessRules> clientAccessRules) {

    public ResourceserverProperties {
        clientAccessRules = List.copyOf(clientAccessRules);
    }

    /**
     * Defines the default security configuration for the resource server.
     *
     * @param securityMatcher the configuration for matching incoming requests
     */
    public record Main(
            SecurityMatcher securityMatcher) {
    }

    /**
     * Defines the request path pattern to which the security configuration applies.
     *
     * @param pathMatchers the request path pattern to match
     */
    public record SecurityMatcher(
            List<String> pathMatchers) {

        public SecurityMatcher {
            pathMatchers = List.copyOf(pathMatchers);
        }
    }

    /**
     * Defines access rules and JWT validation settings for a specific client and request path pattern.
     *
     * @param pathMatchers the request path pattern
     * @param clientId the identifier of the client
     * @param jwt the JWT validation settings
     */
    public record ClientAccessRules(
            String pathMatchers,
            String clientId,
            Jwt jwt) {
    }

    /**
     * Defines the JWT validation settings for a client.
     *
     * @param issuerUri the expected JWT issuer URI
     * @param jwkSetUri the URI of the JWK set used for signature verification
     */
    public record Jwt(
            String issuerUri,
            String jwkSetUri) {
    }

}
