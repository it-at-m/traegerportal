package de.muenchen.rbs.traegerportal.gateway.route;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static de.muenchen.rbs.traegerportal.gateway.TestConstants.SPRING_TEST_PROFILE;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@AutoConfigureWireMock
@Import(OAuthSecurityMockConfiguration.class)
class StammdatenRouteTest {
    private static final String TEST_KEY = "testkey";
    public static final String TEST_VALUE = "testvalue";
    private static final String TEST_JSON = "{ \"" + TEST_KEY + "\" : \"" + TEST_VALUE + "\" }";
    public static final String TEST_KEY_EXPRESSION = "$." + TEST_KEY;

    public static final String URI_EINRICHTUNGEN = "/einrichtungen";

    @Autowired
    private ApplicationContext context;
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        // setup web test client
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
        // setup wiremock for routes
        stubFor(get(urlMatching(".*/einrichtungen"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeaders(new HttpHeaders(
                                new HttpHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json"),
                                new HttpHeader(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE,
                                        "Bearer realm=\"Access to the staging site\", charset=\"UTF-8\"")
                        ))
                        .withBody(TEST_JSON)));
    }

    @Test
    @WithMockUser
    void stammdatenGetSuccess() {
        webTestClient
                .get().uri(URI_EINRICHTUNGEN)
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/hal+json")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json");

        verify(getRequestedFor(urlEqualTo("/einrichtungen"))
                .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, new EqualToPattern("application/hal+json")));
    }

    @Test
    void stammdatenGetForbidden() {
        webTestClient
                .get().uri(URI_EINRICHTUNGEN)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
