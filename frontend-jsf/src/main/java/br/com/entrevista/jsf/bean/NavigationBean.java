package br.com.entrevista.jsf.bean;

import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Named("navigationBean")
@Component
public class NavigationBean {

    private final String angularBaseUrl;

    public NavigationBean(@Value("${app.angular-base-url}") String angularBaseUrl) {
        this.angularBaseUrl = angularBaseUrl.endsWith("/")
                ? angularBaseUrl.substring(0, angularBaseUrl.length() - 1)
                : angularBaseUrl;
    }

    public String getUrlApuracao() {
        return pathAngular("/apuracao-fiscal");
    }

    public String getUrlHistorico() {
        return pathAngular("/historico");
    }

    /** Caminho absoluto no gateway (evita resolver relativo a /faces/...). */
    private String pathAngular(String rota) {
        if (angularBaseUrl == null || angularBaseUrl.isBlank()) {
            return rota;
        }
        return angularBaseUrl + rota;
    }

    public String getUrlDashboard() {
        return "/faces/dashboard.xhtml";
    }

    public String getUrlAliquotas() {
        return "/faces/aliquotas.xhtml";
    }

    public String getUrlLogout() {
        return "/logout";
    }
}
