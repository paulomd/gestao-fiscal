package br.com.entrevista.jsf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String LOGIN_OAUTH2 = "/oauth2/authorization/keycloak";
    private static final String POST_LOGOUT = "{baseUrl}/login-jsf";

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizedClientService authorizedClientService,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/login/**",
                                "/login-jsf",
                                "/oauth2/**",
                                "/error",
                                "/actuator/health/**").permitAll()
                        .requestMatchers(
                                "/jakarta.faces.resource/**",
                                "/javax.faces.resource/**",
                                "/resources/**",
                                "/assets/**").permitAll()
                        .requestMatchers("/*.xhtml", "/faces/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(LOGIN_OAUTH2)))
                .oauth2Login(oauth2 -> oauth2
                        .loginPage(LOGIN_OAUTH2)
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(clientRegistrationRepository)))
                        .successHandler(new OAuth2LoginSuccessHandler(authorizedClientService))
                        .failureHandler((request, response, exception) -> {
                            String context = request.getContextPath();
                            response.sendRedirect(context + LOGIN_OAUTH2);
                        }))
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .addFilterAfter(new RequireOAuth2Filter(), SecurityContextHolderFilter.class);

        return http.build();
    }

    /** Login sempre visível e interface do Keycloak em português (Brasil). */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(keycloakLoginEmPortugues());
        return resolver;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder> keycloakLoginEmPortugues() {
        return customizer -> {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("prompt", "login");
            params.put("ui_locales", "pt-BR");
            customizer.additionalParameters(params);
        };
    }

    @Bean
    LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(POST_LOGOUT);
        return handler;
    }
}
