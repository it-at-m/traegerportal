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
package de.muenchen.rbs.traegerportal.gateway.adapter.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.muenchen.rbs.traegerportal.gateway.adapter.ClientCredentialsAccessTokenProvider;
import de.muenchen.rbs.traegerportal.gateway.adapter.TraegerIdApiRestService;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class TraegerIdApiRestServiceConfiguration {

    @Bean
    public TraegerIdApiRestService traegerIdApiRestService(
            final WebClient.Builder webClientBuilder,
            @Value("${adapter.einrichtungsverwaltung.base-url}") final String evUrl,
            @Value("${adapter.einrichtungsverwaltung.id-resolver.path}") final String idPath,
            final ClientCredentialsAccessTokenProvider tokenProvider,
            @Value("${adapter.einrichtungsverwaltung.id-resolver.cache-expires-after-write}") final Duration cacheExpiresAfterWrite,
            @Value("${adapter.einrichtungsverwaltung.id-resolver.cache-maximum-size}") final int cacheMaximumSize) {
        final WebClient webClient = getWebClient(webClientBuilder, evUrl);

        final Cache<String, Long> idCache = getCache(cacheExpiresAfterWrite, cacheMaximumSize);

        final TraegerIdApiRestService traegerIdApiRestService = new TraegerIdApiRestService(
                webClient,
                idPath,
                idCache,
                tokenProvider);

        log.info(
                "Initialized TraegerIdApiRestService [evUrl = {}, idPath={}, cacheExpiresAfterWrite= {}, cacheMaximumSize={}]",
                evUrl,
                idPath,
                cacheExpiresAfterWrite,
                cacheMaximumSize);

        return traegerIdApiRestService;
    }

    public static WebClient getWebClient(final WebClient.Builder webClientBuilder, final String evUrl) {
        return webClientBuilder
                .baseUrl(evUrl)
                .build();
    }

    public static Cache<String, Long> getCache(final Duration cacheExpiresAfterWrite, final int cacheMaximumSize) {
        return Caffeine.newBuilder()
                .expireAfterWrite(cacheExpiresAfterWrite)
                .maximumSize(cacheMaximumSize)
                .build();
    }

}
