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

    private final String evUrl;
    private final String webcomponentsUrl;

    private final SecurityGatewayFilterFactory gatewayFilterFactory;

    public GatewayConfig(final SecurityGatewayFilterFactory gatewayFilterFactory,
            @Value("${adapter.einrichtungsverwaltung.base-url}") final String evUrl,
            @Value("${adapter.webcomponents.base-url}") final String webcomponentsUrl) {
        log.info("Initializing Gateway with einrichtungsverwaltung-url {} and webcomponents-url {}...", evUrl, webcomponentsUrl);

        this.evUrl = evUrl;
        this.webcomponentsUrl = webcomponentsUrl;
        this.gatewayFilterFactory = gatewayFilterFactory;
    }

    @Bean
    public RouteLocator customRouteLocator(final RouteLocatorBuilder builder) {
        log.info("Configuring routes...");

        return builder.routes()
                .route("einrichtungen", r -> r.path("/einrichtungen")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(gatewayFilterFactory.apply(new SecurityGatewayFilterFactory.Config()))
                                .setPath("/einrichtungen"))
                        .uri(evUrl))
                .route("webcomponents", r -> r.path("/webcomponents/**")
                        .and().method("GET")
                        .filters(f -> f.stripPrefix(1))
                        .uri(webcomponentsUrl))
                .build();
    }
}
