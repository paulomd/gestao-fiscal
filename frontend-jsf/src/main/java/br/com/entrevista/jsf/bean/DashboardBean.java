package br.com.entrevista.jsf.bean;

import br.com.entrevista.jsf.client.ApiClient;
import br.com.entrevista.jsf.client.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;

@Slf4j
@Named("dashboardBean")
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DashboardBean implements Serializable {

    private final ApiClient apiClient;

    @Getter
    private long totalAliquotas;

    @Getter
    private String ultimaVigencia = "-";

    @Getter
    private long totalRegimes;

    @Getter
    private boolean carregado;

    public DashboardBean(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /** Chamado por f:viewAction ao abrir o dashboard (contexto JSF + token na sessão). */
    public void carregar() {
        try {
            String json = apiClient.getRaw("/api/dashboard/stats");
            JsonNode node = apiClient.readTree(json);
            totalAliquotas = node.path("totalAliquotas").asLong(0);
            ultimaVigencia = texto(node, "ultimaVigencia", "-");
            totalRegimes = node.path("totalRegimes").asLong(0);
            carregado = true;
            log.info("Dashboard JSF: {} alíquotas, {} regimes, vigência {}", totalAliquotas, totalRegimes, ultimaVigencia);
        } catch (Exception e) {
            log.warn("Falha ao carregar dashboard JSF", e);
            zerarStats();
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Não foi possível carregar as estatísticas",
                    mensagemErro(e));
        }
    }

    private static String texto(JsonNode node, String campo, String padrao) {
        JsonNode valor = node.path(campo);
        if (valor.isMissingNode() || valor.isNull()) {
            return padrao;
        }
        String t = valor.asText();
        return t != null && !t.isBlank() ? t : padrao;
    }

    private void zerarStats() {
        totalAliquotas = 0;
        totalRegimes = 0;
        ultimaVigencia = "-";
        carregado = false;
    }

    private String mensagemErro(Exception e) {
        if (e instanceof ApiException api && api.getMessage() != null) {
            Throwable cause = api.getCause();
            if (cause != null && cause.getMessage() != null) {
                return api.getMessage() + " (" + cause.getMessage() + ")";
            }
            return api.getMessage();
        }
        return e.getMessage() != null ? e.getMessage() : "Erro ao consultar a API";
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            ctx.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }
}
