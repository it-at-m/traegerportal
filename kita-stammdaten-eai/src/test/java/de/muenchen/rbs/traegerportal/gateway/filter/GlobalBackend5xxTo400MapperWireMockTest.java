package de.muenchen.rbs.traegerportal.gateway.filter;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
        void map_503_response_to_400_and_do_not_pass_original_payload() {
            webTestClient
                    .get().uri("/meintraeger")
                    .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Bad Request")
                    .jsonPath("$.status").isEqualTo("400")
                    .jsonPath("$.testkey").doesNotExist();
        }
    }

    @Nested
    @TestPropertySource(properties = "config.map5xxto400=false")
    class MapTo500 {

        @Test
        void map_503_response_to_500_and_do_not_pass_original_payload() {
            webTestClient
                    .get().uri("/meintraeger")
                    .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                    .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Internal Server Error")
                    .jsonPath("$.status").isEqualTo("500")
                    .jsonPath("$.testkey").doesNotExist();
        }
    }

}
