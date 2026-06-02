package br.com.entrevista.jsf.bean;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("sessionBean")
@Component
@SessionScope
public class SessionBean implements Serializable {

    @Getter
    private String nomeUsuario = "Usuário";

    @Getter
    private String perfil = "FISCAL";

    @Getter
    private String ultimoAcesso;

    @PostConstruct
    public void init() {
        ultimoAcesso = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof OidcUser oidc) {
            nomeUsuario = oidc.getFullName() != null ? oidc.getFullName() : oidc.getPreferredUsername();
            perfil = extractRoles(oidc.getClaims());
        } else if (principal instanceof OAuth2User oauth) {
            Object name = oauth.getAttribute("name");
            nomeUsuario = name != null ? String.valueOf(name) : String.valueOf(oauth.getAttribute("preferred_username"));
            perfil = extractRoles(oauth.getAttributes());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractRoles(Map<String, Object> claims) {
        if (claims == null) {
            return "FISCAL";
        }
        Object realmAccess = claims.get("realm_access");
        if (realmAccess instanceof Map<?, ?> map) {
            Object roles = map.get("roles");
            if (roles instanceof List<?> list) {
                String joined = list.stream()
                        .map(String::valueOf)
                        .filter(r -> "ADMIN".equals(r) || "FISCAL".equals(r))
                        .collect(Collectors.joining(", "));
                return joined.isBlank() ? "FISCAL" : joined;
            }
        }
        return "FISCAL";
    }
}
