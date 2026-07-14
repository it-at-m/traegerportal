package de.muenchen.rbs.traegerportal.gateway.route;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static de.muenchen.rbs.traegerportal.gateway.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@AutoConfigureWireMock(
        stubs = "classpath:/mappings/StammdatenRouteWireMockTest/mappings",
        files = "classpath:/mappings/StammdatenRouteWireMockTest"
)
@Import(OAuthSecurityMockConfiguration.class)
class StammdatenRouteWireMockTest {

    public static final String PATH_MEIN_TRAEGER = "/meintraeger";
    public static final String PATH_EINRICHTUNGEN_FOR_MEIN_TRAEGER = "/meintraeger/einrichtungen";

    @Autowired
    private ApplicationContext context;
    @Autowired
    private OAuthSecurityMockConfiguration oauthSecurityMockConfiguration;
    
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        // setup web test client
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void stammdaten_traeger_get_success() {
        webTestClient
                .get().uri(PATH_MEIN_TRAEGER)
                .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .expectBody()
                .jsonPath("$.id").value(equalTo(1337));
    }

    @Test
    void stammdaten_einrichtungen_for_traeger_get_success() {
        webTestClient
                .get().uri(PATH_EINRICHTUNGEN_FOR_MEIN_TRAEGER)
                .headers(oauthSecurityMockConfiguration::addBackendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .expectBody()
                .jsonPath("$.content[*].id").value(containsInAnyOrder(1337, 1338, 1339));
    }

    @Test
    void stammdaten_traeger_get_forbidden() {
        webTestClient
                .get().uri(PATH_MEIN_TRAEGER)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
