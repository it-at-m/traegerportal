package de.muenchen.rbs.traegerportal.gateway.filter;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static de.muenchen.rbs.traegerportal.gateway.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@AutoConfigureWireMock(
        stubs = "classpath:/mappings/GlobalBackend5xxTo400MapperWireMockTest/mappings",
        files = "classpath:/mappings/GlobalBackend5xxTo400MapperWireMockTest"
)
@TestPropertySource(
        properties = {
                "config.map5xxto400=true",
        }
)
@Import(OAuthSecurityMockConfiguration.class)
class GlobalBackend5xxTo400MapperWireMockTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    OAuthSecurityMockConfiguration oauthSecurityMockConfiguration;

    @Test
    void backendError200() {
        webTestClient.get().uri("/meintraeger")
                .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .expectBody()
                .jsonPath("$.testkey").value(equalTo("testvalue"));
    }

}
