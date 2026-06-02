package br.com.entrevista.jsf.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Garante login OAuth2 nas páginas JSF (evita acesso anônimo ou sessão inválida).
 */
public class RequireOAuth2Filter extends OncePerRequestFilter {

    private static final Pattern PROTECTED =
            Pattern.compile("^/(faces/.*|[^/]+\\.xhtml)$");

    private static final String LOGIN = "/oauth2/authorization/keycloak";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && path.startsWith(context)) {
            path = path.substring(context.length());
        }
        if (path.isEmpty()) {
            path = "/";
        }

        if (PROTECTED.matcher(path).matches() && !isOAuth2LoggedIn()) {
            response.sendRedirect(context + LOGIN);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isOAuth2LoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth instanceof OAuth2AuthenticationToken;
    }
}
