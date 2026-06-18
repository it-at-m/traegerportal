package de.muenchen.rbs.traegerportal.gateway;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import de.muenchen.rbs.traegerportal.gateway.adapter.SecurityGatewayFilterFactory;
import de.muenchen.rbs.traegerportal.gateway.configuration.GatewayConfig;

@TestConfiguration
@Import({GatewayConfig.class, SecurityGatewayFilterFactory.class})
public class OAuthSecurityMockConfiguration {
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return mock(ReactiveJwtDecoder.class);
    }

    @Bean
    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
        return mock(ReactiveClientRegistrationRepository.class);
    }
}
