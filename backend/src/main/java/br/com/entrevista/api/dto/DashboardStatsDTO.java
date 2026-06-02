package br.com.entrevista.api.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    private long totalAliquotas;
    private String ultimaVigencia;
    private long totalRegimes;
    private BigDecimal receitaSimulada;
    private BigDecimal cargaTributariaMedia;
    private List<Map<String, Object>> aliquotasPorRegime;
    private Map<String, BigDecimal> distribuicaoTributos;
}
