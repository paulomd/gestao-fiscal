package br.com.entrevista.jsf.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * Spring Security redireciona falhas OAuth2 para /login?error — esta rota reencaminha ao Keycloak.
 */
@Controller
public class LoginController {

    @GetMapping({"/login", "/login/", "/login-jsf"})
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String context = request.getContextPath();
        response.sendRedirect(context + "/oauth2/authorization/keycloak");
    }
}
