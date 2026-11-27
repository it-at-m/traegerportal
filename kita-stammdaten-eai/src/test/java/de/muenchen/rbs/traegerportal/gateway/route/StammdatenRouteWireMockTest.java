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
package de.muenchen.rbs.traegerportal.gateway.route;

import static de.muenchen.rbs.traegerportal.gateway.TestConstants.SPRING_TEST_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@EnableWireMock(
    @ConfigureWireMock(
            filesUnderDirectory = "src/test/resources/mappings/StammdatenRouteWireMockTest"
    )
)
@Import(OAuthSecurityMockConfiguration.class)
class StammdatenRouteWireMockTest {

    public static final String PATH_MEIN_TRAEGER = "/meintraeger";
    public static final String PATH_EINRICHTUNGEN_FOR_MEIN_TRAEGER = "/meintraeger/einrichtungen";
    public static final String PATH_MEINE_VORGAENGE = "/meinevorgaenge";

    @Autowired
    private OAuthSecurityMockConfiguration oauthSecurityMockConfiguration;
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void stammdaten_traeger_get_success() {
        webTestClient
                .get().uri(PATH_MEIN_TRAEGER)
                .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1337);
    }

    @Test
    void stammdaten_traeger_get_forbidden() {
        webTestClient
                .get().uri(PATH_MEIN_TRAEGER)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void stammdaten_einrichtungen_for_traeger_get_success() {
        webTestClient
                .get().uri(PATH_EINRICHTUNGEN_FOR_MEIN_TRAEGER)
                .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content[*].id").value(ids -> assertThat(ids).asInstanceOf(InstanceOfAssertFactories.LIST)
                        .containsExactlyInAnyOrder(1337, 1338, 1339));
    }

    @Test
    void stammdaten_einrichtungen_for_traeger_get_forbidden() {
        webTestClient
                .get().uri(PATH_EINRICHTUNGEN_FOR_MEIN_TRAEGER)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void stammdaten_vorgaenge_for_traeger_get_success() {
        webTestClient
                .get().uri(PATH_MEINE_VORGAENGE)
                .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content[*].aktenzeichen").value(ids -> assertThat(ids).asInstanceOf(InstanceOfAssertFactories.LIST)
                        .containsExactlyInAnyOrder("EA-0000001", "EA-0000002", "TA-0000001"));
    }

    @Test
    void stammdaten_vorgaenge_get_success() {
        webTestClient
                .get().uri(PATH_MEINE_VORGAENGE)
                .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content[*].aktenzeichen").value(ids -> assertThat(ids).asInstanceOf(InstanceOfAssertFactories.LIST)
                        .containsExactlyInAnyOrder("EA-0000001", "EA-0000002", "TA-0000001"));
    }
}
