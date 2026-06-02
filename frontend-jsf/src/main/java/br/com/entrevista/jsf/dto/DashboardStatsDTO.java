package br.com.entrevista.jsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardStatsDTO {

    private long totalAliquotas;
    private String ultimaVigencia;
    private long totalRegimes;
    private BigDecimal receitaSimulada;
    private BigDecimal cargaTributariaMedia;
    private List<RegimeItem> aliquotasPorRegime = new ArrayList<>();
    private Map<String, BigDecimal> distribuicaoTributos = new HashMap<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegimeItem {
        private String regime;
        private long quantidade;
    }
}
