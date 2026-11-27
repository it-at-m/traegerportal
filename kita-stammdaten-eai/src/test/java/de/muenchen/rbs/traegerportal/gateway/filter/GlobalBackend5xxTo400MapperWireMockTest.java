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
package de.muenchen.rbs.traegerportal.gateway.filter;

import static de.muenchen.rbs.traegerportal.gateway.TestConstants.SPRING_TEST_PROFILE;
import static de.muenchen.rbs.traegerportal.gateway.filter.GlobalBackend5xxTo400Mapper.GENERIC_ERROR_400;
import static de.muenchen.rbs.traegerportal.gateway.filter.GlobalBackend5xxTo400Mapper.GENERIC_ERROR_500;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@EnableWireMock(
    @ConfigureWireMock(
            filesUnderDirectory = "src/test/resources/mappings/GlobalBackend5xxTo400MapperWireMockTest"
    )
)
@Import({ OAuthSecurityMockConfiguration.class })
class GlobalBackend5xxTo400MapperWireMockTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private OAuthSecurityMockConfiguration oauthSecurityMockConfiguration;

    @Nested
    @TestPropertySource(properties = "config.map5xxto400=true")
    class MapTo400 {

        @Test
        void map_503Response_to_400Response_and_do_not_pass_original_payload() {
            webTestClient
                    .get().uri("/meintraeger")
                    .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(String.class)
                    .isEqualTo(GENERIC_ERROR_400);
        }
    }

    @Nested
    @TestPropertySource(properties = "config.map5xxto400=false")
    class MapTo500 {

        @Test
        void map_503Response_to_500Response_and_do_not_pass_original_payload() {
            webTestClient
                    .get().uri("/meintraeger")
                    .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(String.class)
                    .isEqualTo(GENERIC_ERROR_500);
        }
    }

}
