package br.com.entrevista.jsf.bean;

import br.com.entrevista.jsf.client.ApiClient;
import br.com.entrevista.jsf.client.ApiException;
import br.com.entrevista.jsf.dto.AliquotaDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Named("aliquotaBean")
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AliquotaBean implements Serializable {

    public static final String MODO_NOVO = "novo";
    public static final String MODO_EDITAR = "editar";

    private final ApiClient apiClient;

    @Getter
    private List<AliquotaDTO> aliquotas = new ArrayList<>();

    @Getter
    @Setter
    private String filtro;

    @Getter
    @Setter
    private AliquotaDTO selecionada = new AliquotaDTO();

    @Getter
    @Setter
    private String modoFormulario;

    @Getter
    @Setter
    private String vigenciaInput;

    @Getter
    @Setter
    private String pisText;

    @Getter
    @Setter
    private String cofinsText;

    @Getter
    @Setter
    private String irpjText;

    @Getter
    @Setter
    private String csllText;

    @Getter
    private final List<String> regimes = List.of(
            "Simples Nacional", "Lucro Presumido", "Lucro Real");

    public AliquotaBean(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @PostConstruct
    public void init() {
        listar();
    }

    public void carregarPagina() {
        Map<String, String> params = parametrosRequisicao();
        if ("1".equals(params.get("fechar"))) {
            fecharFormulario();
            return;
        }
        listar();
        if ("1".equals(params.get("novo"))) {
            abrirNovo();
            return;
        }
        String editarId = params.get("editar");
        if (editarId != null && !editarId.isBlank()) {
            try {
                Long id = Long.parseLong(editarId.trim());
                aliquotas.stream()
                        .filter(a -> id.equals(a.getId()))
                        .findFirst()
                        .ifPresent(this::editarInterno);
            } catch (NumberFormatException e) {
                log.warn("Parâmetro editar inválido: {}", editarId);
            }
        }
    }

    public boolean isExibirFormulario() {
        if (MODO_NOVO.equals(modoFormulario) || MODO_EDITAR.equals(modoFormulario)) {
            return true;
        }
        Map<String, String> params = parametrosRequisicao();
        if ("1".equals(params.get("novo"))) {
            return true;
        }
        String editar = params.get("editar");
        return editar != null && !editar.isBlank();
    }

    public String getUrlNovaAliquota() {
        return urlAliquotas("novo=1");
    }

    public String getUrlFecharFormulario() {
        return urlAliquotas("fechar=1");
    }

    private String urlAliquotas(String query) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String base = ctx != null ? ctx.getExternalContext().getRequestContextPath() : "";
        if (query == null || query.isBlank()) {
            return base + "/faces/aliquotas.xhtml";
        }
        return base + "/faces/aliquotas.xhtml?" + query;
    }

    public void abrirNovo() {
        modoFormulario = MODO_NOVO;
        resetFormulario();
    }

    public void fecharFormulario() {
        modoFormulario = null;
        resetFormulario();
    }

    public void listar() {
        try {
            String path = filtro != null && !filtro.isBlank()
                    ? "/api/aliquotas?filtro=" + java.net.URLEncoder.encode(filtro, java.nio.charset.StandardCharsets.UTF_8)
                    : "/api/aliquotas";
            aliquotas = apiClient.getList(path, AliquotaDTO.class);
        } catch (Exception e) {
            log.warn("Erro ao listar: {}", e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao listar alíquotas", mensagem(e));
        }
    }

    public void salvar() {
        FacesContext faces = FacesContext.getCurrentInstance();
        if (faces.isValidationFailed()) {
            faces.getMessageList().forEach(m ->
                    addMessage(m.getSeverity(), m.getSummary(), m.getDetail()));
            if (faces.getMessageList().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Validação", "Corrija os campos destacados");
            }
            return;
        }
        try {
            if (modoFormulario == null || modoFormulario.isBlank()) {
                modoFormulario = MODO_NOVO;
            }
            aplicarCamposDoFormulario();
            validarFormulario();
            selecionada.setDataCadastro(null);
            log.info("Salvando alíquota: regime={}, vigencia={}, pis={}", selecionada.getRegimeTributario(),
                    selecionada.getVigencia(), selecionada.getPis());
            if (selecionada.getId() == null) {
                apiClient.post("/api/aliquotas", selecionada, AliquotaDTO.class);
                addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Alíquota cadastrada com sucesso");
            } else {
                apiClient.put("/api/aliquotas/" + selecionada.getId(), selecionada, AliquotaDTO.class);
                addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Alíquota atualizada com sucesso");
            }
            modoFormulario = null;
            resetFormulario();
            listar();
        } catch (Exception e) {
            log.error("Erro ao salvar alíquota", e);
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", mensagem(e));
        }
    }

    public void excluir(AliquotaDTO item) {
        try {
            apiClient.delete("/api/aliquotas/" + item.getId());
            addMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Alíquota excluída");
            listar();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem(e));
        }
    }

    public void editar(AliquotaDTO item) {
        editarInterno(item);
    }

    private void editarInterno(AliquotaDTO item) {
        selecionada = copiar(item);
        sincronizarCamposTexto();
        modoFormulario = MODO_EDITAR;
    }

    private void aplicarCamposDoFormulario() {
        aplicarRegimeDoRequest();
        selecionada.setPis(parseDecimal(pisText, "PIS"));
        selecionada.setCofins(parseDecimal(cofinsText, "COFINS"));
        selecionada.setIrpj(parseDecimal(irpjText, "IRPJ"));
        selecionada.setCsll(parseDecimal(csllText, "CSLL"));
        aplicarVigenciaDoInput();
    }

    private void aplicarRegimeDoRequest() {
        if (selecionada.getRegimeTributario() != null && !selecionada.getRegimeTributario().isBlank()) {
            return;
        }
        for (var entry : parametrosRequisicao().entrySet()) {
            if (entry.getKey().contains("regime")) {
                String valor = entry.getValue();
                if (valor != null && !valor.isBlank()) {
                    selecionada.setRegimeTributario(valor);
                    return;
                }
            }
        }
    }

    private BigDecimal parseDecimal(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " inválido — use números com ponto (ex.: 1.65)");
        }
    }

    private void resetFormulario() {
        selecionada = new AliquotaDTO();
        selecionada.setVigencia(LocalDate.now());
        selecionada.setPis(BigDecimal.ZERO);
        selecionada.setCofins(BigDecimal.ZERO);
        selecionada.setIrpj(BigDecimal.ZERO);
        selecionada.setCsll(BigDecimal.ZERO);
        sincronizarCamposTexto();
    }

    private void sincronizarCamposTexto() {
        vigenciaInput = selecionada.getVigencia() != null
                ? selecionada.getVigencia().toString()
                : LocalDate.now().toString();
        pisText = decimalParaTexto(selecionada.getPis());
        cofinsText = decimalParaTexto(selecionada.getCofins());
        irpjText = decimalParaTexto(selecionada.getIrpj());
        csllText = decimalParaTexto(selecionada.getCsll());
    }

    private String decimalParaTexto(BigDecimal valor) {
        return valor != null ? valor.stripTrailingZeros().toPlainString() : "0";
    }

    private void aplicarVigenciaDoInput() {
        if (vigenciaInput == null || vigenciaInput.isBlank()) {
            selecionada.setVigencia(null);
            return;
        }
        try {
            selecionada.setVigencia(LocalDate.parse(vigenciaInput.trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Vigência inválida — use o formato AAAA-MM-DD");
        }
    }

    private void validarFormulario() {
        if (selecionada.getRegimeTributario() == null || selecionada.getRegimeTributario().isBlank()) {
            throw new IllegalArgumentException("Selecione o regime tributário");
        }
        if (selecionada.getVigencia() == null) {
            throw new IllegalArgumentException("Informe a vigência");
        }
        garantirDecimal(selecionada.getPis());
        garantirDecimal(selecionada.getCofins());
        garantirDecimal(selecionada.getIrpj());
        garantirDecimal(selecionada.getCsll());
    }

    private void garantirDecimal(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Preencha todos os percentuais");
        }
    }

    private Map<String, String> parametrosRequisicao() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            return Map.of();
        }
        return ctx.getExternalContext().getRequestParameterMap();
    }

    private AliquotaDTO copiar(AliquotaDTO origem) {
        AliquotaDTO dto = new AliquotaDTO();
        dto.setId(origem.getId());
        dto.setRegimeTributario(origem.getRegimeTributario());
        dto.setPis(origem.getPis());
        dto.setCofins(origem.getCofins());
        dto.setIrpj(origem.getIrpj());
        dto.setCsll(origem.getCsll());
        dto.setVigencia(origem.getVigencia());
        dto.setDataCadastro(origem.getDataCadastro());
        return dto;
    }

    private String mensagem(Exception e) {
        if (e instanceof ApiException api && api.getMessage() != null) {
            return api.getMessage();
        }
        Throwable cause = e.getCause();
        if (cause != null && cause.getMessage() != null) {
            return cause.getMessage();
        }
        return e.getMessage() != null ? e.getMessage() : "Erro inesperado";
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
