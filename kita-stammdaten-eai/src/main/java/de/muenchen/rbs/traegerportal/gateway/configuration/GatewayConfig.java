package de.muenchen.rbs.traegerportal.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GatewayConfig {

    private String evUrl;

    private final SecurityGatewayFilterFactory gatewayFilterFactory;

    public GatewayConfig(SecurityGatewayFilterFactory gatewayFilterFactory,
            @Value("${adapter.einrichtungsverwaltung.base-url}") String evUrl) {
        log.info("Initializing Gateway with einrichtungsverwaltung-url {}...", evUrl);

        this.evUrl = evUrl;
        this.gatewayFilterFactory = gatewayFilterFactory;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("traegerdaten", r -> r.path("/traegerdaten")
                        .and().method("GET")
                        .filters(f -> f.filter(gatewayFilterFactory.apply(new SecurityGatewayFilterFactory.Config()))
                                .setPath("/"))
                        .uri(evUrl + "/traegerdaten/"))
                .route("einrichtungen", r -> r.path("/einrichtungen")
                        .and().method("GET")
                        .filters(f -> f.filter(gatewayFilterFactory.apply(new SecurityGatewayFilterFactory.Config()))
                                .setPath("/"))
                        .uri(evUrl + "/einrichtungen/"))
                .build();
    }
}