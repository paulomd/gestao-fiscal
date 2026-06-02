package br.com.entrevista.jsf.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Guarda o access token na sessão HTTP para chamadas RestTemplate a partir do JSF.
 */
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String SESSION_ACCESS_TOKEN = "API_ACCESS_TOKEN";

    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2LoginSuccessHandler(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        setDefaultTargetUrl("/faces/dashboard.xhtml");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauth.getAuthorizedClientRegistrationId(), oauth.getName());
            if (client != null && client.getAccessToken() != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute(SESSION_ACCESS_TOKEN, client.getAccessToken().getTokenValue());
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
