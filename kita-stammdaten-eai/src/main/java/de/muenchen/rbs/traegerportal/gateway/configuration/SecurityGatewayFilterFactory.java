package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityGatewayFilterFactory extends AbstractGatewayFilterFactory<SecurityGatewayFilterFactory.Config> {

    private final String UK_ID_CLAIM = "datenuebermittlerPseudonymId";
    
    @Autowired
    private JwtDecoder jwtDecoder; 
    
    public SecurityGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwtToken = authorizationHeader.substring(7);

                String ukId = extractUkIdFromToken(jwtToken);

                URI uri = exchange.getRequest().getURI();
                String newUri = uri.toString() + (uri.getQuery() == null ? "?" : "&") + "ukId=" + ukId;
                
                exchange.getRequest().mutate()
                        .uri(URI.create(newUri))
                        .header("Authorization", "TODO")
                        .build();
            }

            return chain.filter(exchange);
        };
    }

    private String extractUkIdFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaimAsString(UK_ID_CLAIM);
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    public static class Config {
        // Add any configuration properties if needed
    }
}