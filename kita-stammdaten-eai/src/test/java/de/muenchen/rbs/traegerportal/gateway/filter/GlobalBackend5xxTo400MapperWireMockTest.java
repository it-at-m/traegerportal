package de.muenchen.rbs.traegerportal.gateway.filter;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static de.muenchen.rbs.traegerportal.gateway.TestConstants.SPRING_TEST_PROFILE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@EnableWireMock(
        @ConfigureWireMock(
                filesUnderDirectory = "src/test/resources/mappings/GlobalBackend5xxTo400MapperWireMockTest"
        )
)
@Import(OAuthSecurityMockConfiguration.class)
class GlobalBackend5xxTo400MapperWireMockTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private OAuthSecurityMockConfiguration oauthSecurityMockConfiguration;

    @Nested
    @TestPropertySource(properties = "config.map5xxto400=true")
    class MapTo400 {

        @Test
        void map_503_response_to_400_and_drop_payload() {
            webTestClient
                    .get().uri("/meintraeger")
                    .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .expectBody()
                    .jsonPath("$.testkey").doesNotExist()
            ;
        }
    }

    @Nested
    @TestPropertySource(properties = "config.map5xxto400=false")
    class MapTo500 {

        @Test
        void preserve_503_response_and_payload() {
            webTestClient
                    .get().uri("/meintraeger")
                    .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                    .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .expectBody()
                    .jsonPath("$.testkey").isEqualTo("testvalue")
            ;
        }
    }

}
