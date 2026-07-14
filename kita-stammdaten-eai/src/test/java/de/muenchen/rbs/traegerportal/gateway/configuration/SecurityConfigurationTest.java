package de.muenchen.rbs.traegerportal.gateway.configuration;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import de.muenchen.rbs.traegerportal.gateway.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { TestConstants.SPRING_TEST_PROFILE })
@AutoConfigureObservability
@Import(OAuthSecurityMockConfiguration.class)
class SecurityConfigurationTest {
    @Autowired
    private WebTestClient api;

    @Test
    void accessSecuredResourceRootThenUnauthorized() {
        api.get().uri("/").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredMeinTraegerThenUnauthorized() {
        api.get().uri("/meintraeger").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredMeinTraegerEinrichtungenThenUnauthorized() {
        api.get().uri("/meintraeger/einrichtungen").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthThenOk() {
        api.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthLivenessThenOk() {
        api.get().uri("/actuator/health/liveness").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthReadinessThenOk() {
        api.get().uri("/actuator/health/readiness").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorInfoThenOk() {
        api.get().uri("/actuator/info").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorMetricsThenOk() {
        api.get().uri("/actuator/metrics").exchange().expectStatus().isOk();
    }
}
