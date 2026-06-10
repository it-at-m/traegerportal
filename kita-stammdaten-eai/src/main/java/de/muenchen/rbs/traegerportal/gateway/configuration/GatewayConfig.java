package de.muenchen.rbs.traegerportal.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    
    @Value("app.einrichtungsverwaltung-url")
    private String backendUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("traegerdaten", r -> r.path("/traegerdaten")
                    .and().method("GET")
                    .uri(backendUrl + "/traegerdaten"))
            .route("einrichtungen", r -> r.path("/einrichtungen")
                    .and().method("GET")
                    .uri(backendUrl + "/einrichtungen"))
            .build();
    }
}