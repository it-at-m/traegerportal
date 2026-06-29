package de.muenchen.rbs.traegerportal.gateway.configuration;

import de.muenchen.rbs.traegerportal.gateway.adapter.SecurityGatewayFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        log.info("Configuring routes...");

        return builder.routes()
                .route("einrichtungen", r -> r.path("/einrichtungen")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(gatewayFilterFactory.apply(new SecurityGatewayFilterFactory.Config()))
                                .setPath("/einrichtungen"))
                        .uri(evUrl))
                .build();
    }
}
