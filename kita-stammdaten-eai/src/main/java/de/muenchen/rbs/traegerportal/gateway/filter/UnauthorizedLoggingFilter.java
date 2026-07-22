package de.muenchen.rbs.traegerportal.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UnauthorizedLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            if (exchange.getResponse().getStatusCode() != null &&
                    exchange.getResponse().getStatusCode().value() == 401) {
                final String path = exchange.getRequest().getURI().getPath();
                log.warn("Unauthorized access attempt to path: {}", path);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Run after response status is set
    }
}
