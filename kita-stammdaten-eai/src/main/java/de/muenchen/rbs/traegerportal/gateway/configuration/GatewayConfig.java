package de.muenchen.rbs.traegerportal.gateway.configuration;

import de.muenchen.rbs.traegerportal.gateway.adapter.StammdatenSecurityGatewayFilterFactory;
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
    private final String evTraegerBasePath;
    
    private final StammdatenSecurityGatewayFilterFactory gatewayFilterFactory;

    public GatewayConfig(final StammdatenSecurityGatewayFilterFactory gatewayFilterFactory,
            @Value("${adapter.einrichtungsverwaltung.base-url}") final String evUrl,
            @Value("${adapter.webcomponents.base-url}") final String webcomponentsUrl,
            @Value("${adapter.einrichtungsverwaltung.base-path}")final String evTraegerBasePath) {
        log.info("Initializing Gateway with einrichtungsverwaltung-url {}, path {} and webcomponents-url {}...", evUrl, evTraegerBasePath, webcomponentsUrl);

        this.evUrl = evUrl;
        this.webcomponentsUrl = webcomponentsUrl;
        this.evTraegerBasePath = evTraegerBasePath;
        this.gatewayFilterFactory = gatewayFilterFactory;
    }

    @Bean
    public RouteLocator customRouteLocator(final RouteLocatorBuilder builder) {
        log.info("Configuring routes...");

        return builder.routes()
                .route("meintraeger", r -> r.path("/meintraeger", "/meintraeger/**")
                        .and().method("GET")
                        .filters(f -> f
                                .rewritePath("^/meintraeger", evTraegerBasePath)
                                .filter(gatewayFilterFactory.apply(new StammdatenSecurityGatewayFilterFactory.Config())))
                        .uri(evUrl))
                .route("webcomponents", r -> r.path("/webcomponents/**")
                        .and().method("GET")
                        .filters(f -> f.stripPrefix(1))
                        .uri(webcomponentsUrl))
                .build();
    }
}
