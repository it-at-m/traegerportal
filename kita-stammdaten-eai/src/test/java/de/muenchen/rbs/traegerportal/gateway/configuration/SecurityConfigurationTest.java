/*
 * The MIT License
 * Copyright © 2026 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

    // TODO: Discuss
    //    @Test
    //    void accessSecuredResourceRootThenUnauthorized() {
    //        webTestClient.get().uri("/").exchange().expectStatus().isUnauthorized();
    //    }

    @Test
    void accessSecuredMeinTraegerThenUnauthorized() {
        webTestClient.get().uri("/meintraeger").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredMeinTraegerEinrichtungenThenUnauthorized() {
        webTestClient.get().uri("/meintraeger/einrichtungen").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredMeineVorgaengeThenUnauthorized() {
        webTestClient.get().uri("/meinevorgaenge").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredEgovValidierungsEaiSearchTraegerThenUnauthorized() {
        webTestClient.get().uri("/clients/egovfor-validierungs-eai/search/traeger").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredEgovValidierungsEaiSearchEinrichtungThenUnauthorized() {
        webTestClient.get().uri("/clients/egovfor-validierungs-eai/search/einrichtung").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessSecuredEgovValidierungsEaiSearchAnsprechpartnerThenUnauthorized() {
        webTestClient.get().uri("/clients/egovfor-validierungs-eai/search/ansprechpartner").exchange().expectStatus().isUnauthorized();
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
