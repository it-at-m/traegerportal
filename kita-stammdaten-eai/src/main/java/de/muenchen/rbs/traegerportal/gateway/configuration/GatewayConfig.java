package de.muenchen.rbs.traegerportal.gateway.configuration;

import de.muenchen.rbs.traegerportal.gateway.adapter.StammdatenSecurityGatewayFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Slf4j
@Configuration
public class GatewayConfig {

    private final String evUrl;
    private final String fbUrl;
    private final String webcomponentsUrl;
    private final String evTraegerBasePath;
    private final String fbBasePath;

    private final StammdatenSecurityGatewayFilterFactory gatewayFilterFactory;

    public GatewayConfig(final StammdatenSecurityGatewayFilterFactory gatewayFilterFactory,
            @Value("${adapter.einrichtungsverwaltung.base-url}") final String evUrl,
            @Value("${adapter.fallbearbeitung.base-url}") final String fbUrl,
            @Value("${adapter.webcomponents.base-url}") final String webcomponentsUrl,
            @Value("${adapter.einrichtungsverwaltung.base-path}") final String evTraegerBasePath,
            @Value("${adapter.fallbearbeitung.base-path}") final String fbBasePath) {
        log.info("Initializing Gateway with einrichtungsverwaltung-url {}, path {}, fallbearbeitung-url {}, path {} and webcomponents-url {}...", evUrl, evTraegerBasePath, fbUrl, fbBasePath, webcomponentsUrl);

        this.evUrl = evUrl;
        this.fbUrl = fbUrl;
        this.webcomponentsUrl = webcomponentsUrl;
        this.evTraegerBasePath = evTraegerBasePath;
        this.fbBasePath = fbBasePath;
        this.gatewayFilterFactory = gatewayFilterFactory;
    }

    @Bean
    public RouteLocator customRouteLocator(final RouteLocatorBuilder builder) {
        log.info("Configuring routes...");

        return builder.routes()
                .route("meintraeger", r -> r.path("/meintraeger", "/meintraeger/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f
                                .rewritePath("^/meintraeger", evTraegerBasePath)
                                .removeRequestHeader("Origin")
                                .filter(gatewayFilterFactory.apply(new StammdatenSecurityGatewayFilterFactory.Config())))
                        .uri(evUrl))
                .route("meinevorgaenge", r -> r.path("/meinevorgaenge", "/meinevorgaenge/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f
                                .rewritePath("^/meinevorgaenge", fbBasePath)
                                .removeRequestHeader("Origin")
                                .filter(gatewayFilterFactory.apply(new StammdatenSecurityGatewayFilterFactory.Config())))
                        .uri(fbUrl))
                .route("webcomponents", r -> r.path("/webcomponents/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.stripPrefix(1))
                        .uri(webcomponentsUrl))
                .build();
    }
}
