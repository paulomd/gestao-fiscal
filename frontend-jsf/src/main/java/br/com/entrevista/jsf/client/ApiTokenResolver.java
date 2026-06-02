package br.com.entrevista.jsf.client;

import br.com.entrevista.jsf.config.OAuth2LoginSuccessHandler;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
@Component
public class ApiTokenResolver {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public ApiTokenResolver(
            OAuth2AuthorizedClientManager authorizedClientManager,
            OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientService = authorizedClientService;
    }

    public String resolveToken() {
        String fromSession = tokenDaSessao();
        if (fromSession != null && !fromSession.isBlank()) {
            return fromSession;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof OAuth2AuthenticationToken oauth)) {
            throw new ApiException("Usuário não autenticado via OAuth2");
        }

        OAuth2AuthorizedClient fromClient = authorizedClientService.loadAuthorizedClient(
                oauth.getAuthorizedClientRegistrationId(), oauth.getName());
        if (fromClient != null && fromClient.getAccessToken() != null) {
            String token = fromClient.getAccessToken().getTokenValue();
            gravarTokenNaSessao(token);
            return token;
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(oauth.getAuthorizedClientRegistrationId())
                .principal(oauth)
                .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);
        String token = Optional.ofNullable(client)
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue)
                .orElseThrow(() -> new ApiException("Token OAuth2 indisponível — faça logout e login novamente"));
        gravarTokenNaSessao(token);
        log.debug("Token OAuth2 obtido via AuthorizedClientManager");
        return token;
    }

    private String tokenDaSessao() {
        HttpSession session = sessaoAtual();
        if (session == null) {
            return null;
        }
        Object token = session.getAttribute(OAuth2LoginSuccessHandler.SESSION_ACCESS_TOKEN);
        return token != null ? token.toString() : null;
    }

    private void gravarTokenNaSessao(String token) {
        HttpSession session = sessaoAtual();
        if (session != null && token != null) {
            session.setAttribute(OAuth2LoginSuccessHandler.SESSION_ACCESS_TOKEN, token);
        }
    }

    private HttpSession sessaoAtual() {
        HttpServletRequest request = requisicaoAtual();
        return request != null ? request.getSession(false) : null;
    }

    public HttpServletRequest requisicaoAtual() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest();
        }
        FacesContext faces = FacesContext.getCurrentInstance();
        if (faces != null) {
            Object req = faces.getExternalContext().getRequest();
            if (req instanceof HttpServletRequest http) {
                return http;
            }
        }
        return null;
    }
}
