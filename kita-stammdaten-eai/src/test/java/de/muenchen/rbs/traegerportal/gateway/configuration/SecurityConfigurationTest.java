package de.muenchen.rbs.traegerportal.gateway.configuration;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import de.muenchen.rbs.traegerportal.gateway.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { TestConstants.SPRING_TEST_PROFILE })
@AutoConfigureTracing
@AutoConfigureMetrics
@Import(OAuthSecurityMockConfiguration.class)
class SecurityConfigurationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void accessSecuredResourceRootThenUnauthorized() {
        webTestClient.get().uri("/").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredMeinTraegerThenUnauthorized() {
        webTestClient.get().uri("/meintraeger").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredMeinTraegerEinrichtungenThenUnauthorized() {
        webTestClient.get().uri("/meintraeger/einrichtungen").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthThenOk() {
        webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthLivenessThenOk() {
        webTestClient.get().uri("/actuator/health/liveness").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthReadinessThenOk() {
        webTestClient.get().uri("/actuator/health/readiness").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorInfoThenOk() {
        webTestClient.get().uri("/actuator/info").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorMetricsThenOk() {
        webTestClient.get().uri("/actuator/metrics").exchange().expectStatus().isOk();
    }
}
