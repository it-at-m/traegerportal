package de.muenchen.rbs.traegerportal.gateway.filter;

import static de.muenchen.rbs.traegerportal.gateway.TestConstants.SPRING_TEST_PROFILE;

import de.muenchen.rbs.traegerportal.gateway.OAuthSecurityMockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@Import(OAuthSecurityMockConfiguration.class)
class GlobalRequestParameterPollutionFilterTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private OAuthSecurityMockConfiguration oauthSecurityMockConfiguration;

    @Test
    void parameterPollutionAttack() {
        final String url = "/meintraeger?parameter1=testdata_1&parameter2=testdata&parameter1=testdata_2";

        webTestClient
                .get().uri(url)
                .headers(oauthSecurityMockConfiguration::addFrontendBearerAuth)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
