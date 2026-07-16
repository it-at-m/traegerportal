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
    void stammdaten_traeger_get_forbidden() {
        webTestClient
                .get().uri(PATH_MEIN_TRAEGER)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
