package de.muenchen.rbs.traegerportal.gateway.configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@Profile("!no-security")
@RequiredArgsConstructor
public class SecurityConfiguration {
	private static final String CLIENT_ROUTES_PREFIX = "/clients/**";
	private static final String PUBLIC_ROUTES_PREFIX = "/public/**";

	private final CsrfProtectionMatcher csrfProtectionMatcher;
	private final SessionProperties sessionProperties;
	private final ServerProperties serverProperties;
	private final SecurityProperties securityProperties;

	@Bean
	@Order(0)
	public SecurityWebFilterChain clientAccessFilterChain(final ServerHttpSecurity http) {
		// build patterns
		final List<String> patternsList = new ArrayList<>();
		patternsList.add(CLIENT_ROUTES_PREFIX);
		if (!securityProperties.getClientPatterns().isEmpty()) {
			patternsList.addAll(securityProperties.getClientPatterns());
		}
		final String[] patterns = patternsList.toArray(new String[0]);
		// security config
		http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(patterns))
				.authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
						.pathMatchers(HttpMethod.OPTIONS, patterns).permitAll().anyExchange().authenticated())
				.cors(corsSpec -> {
				}).oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
	
	/**
	 * Apply dynamic permitAll rules based on properties.
	 */
	private void applyDynamicPermitAll(final ServerHttpSecurity.AuthorizeExchangeSpec authorize) {
		for (final SecurityProperties.PermitRule rule : securityProperties.getPublicPatterns()) {
			for (final HttpMethod method : rule.getMethods()) {
				authorize.pathMatchers(method, rule.getPattern()).permitAll();
			}
		}
	}

	/**
	 * Get Spring Session timeout. Uses {@link SessionProperties} and
	 * {@link ServerProperties#getServlet()} as fallback, like Spring Session
	 * itself. See according <a href=
	 * "https://docs.spring.io/spring-boot/reference/web/spring-session.html">Spring
	 * documentation</a>.
	 *
	 * @return Spring session timeout.
	 */
	protected Duration getSessionTimeout() {
		return sessionProperties.determineTimeout(() -> serverProperties.getServlet().getSession().getTimeout());
	}
	
	

	/**
	 * Registers a JWT Decoder
	 * 
	 * @param issuerUri        expected issuer uri
	 * @param jwkUri           jwk uri to validate jwt
	 * @param requiredAudience expected audience
	 * @return the configured JWTDecoder
	 */
	@Bean(name = "stammdatenJwtDecoder")
	public ReactiveJwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
			@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkUri,
			@Value("${spring.security.oauth2.resourceserver.jwt.audience}") String requiredAudience) {
	    NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkUri).build();

		// Create validators for the JWT
		OAuth2TokenValidator<Jwt> audienceValidator = JwtValidators.createDefaultWithValidators(
				jwt -> jwt.getAudience().contains(requiredAudience) ? OAuth2TokenValidatorResult.success()
						: OAuth2TokenValidatorResult.failure());
		OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
		OAuth2TokenValidator<Jwt> combined = new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);

		// Set the validators on the decoder
		jwtDecoder.setJwtValidator(combined);
		return jwtDecoder;
	}
}
