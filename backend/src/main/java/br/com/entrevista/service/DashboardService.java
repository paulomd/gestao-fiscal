package br.com.entrevista.service;

import br.com.entrevista.api.dto.DashboardStatsDTO;
import br.com.entrevista.domain.entity.Aliquota;
import br.com.entrevista.domain.entity.ApuracaoFiscal;
import br.com.entrevista.domain.repository.AliquotaRepository;
import br.com.entrevista.domain.repository.ApuracaoFiscalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AliquotaRepository aliquotaRepository;
    private final ApuracaoFiscalRepository apuracaoRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO obterEstatisticas() {
        List<Aliquota> aliquotas = aliquotaRepository.findAll();
        List<ApuracaoFiscal> apuracoes = apuracaoRepository.findAll();

        String ultimaVigencia = aliquotas.stream()
                .map(Aliquota::getVigencia)
                .max(Comparator.naturalOrder())
                .map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .orElse("-");

        long regimes = aliquotas.stream()
                .map(Aliquota::getRegimeTributario)
                .distinct()
                .count();

        List<Map<String, Object>> porRegime = aliquotas.stream()
                .collect(Collectors.groupingBy(Aliquota::getRegimeTributario))
                .entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "regime", e.getKey(),
                        "quantidade", e.getValue().size()))
                .toList();

        Map<String, BigDecimal> distribuicao = new LinkedHashMap<>();
        if (!aliquotas.isEmpty()) {
            Aliquota media = aliquotas.getFirst();
            distribuicao.put("PIS", media.getPis());
            distribuicao.put("COFINS", media.getCofins());
            distribuicao.put("IRPJ", media.getIrpj());
            distribuicao.put("CSLL", media.getCsll());
        }

        BigDecimal receitaSimulada = apuracoes.stream()
                .map(ApuracaoFiscal::getReceitaBruta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (receitaSimulada.compareTo(BigDecimal.ZERO) == 0) {
            receitaSimulada = BigDecimal.valueOf(100000);
        }

        BigDecimal cargaMedia = apuracoes.isEmpty()
                ? BigDecimal.valueOf(33.25)
                : apuracoes.stream()
                        .map(ApuracaoFiscal::getCargaTributaria)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(apuracoes.size()), 2, RoundingMode.HALF_UP);

        return DashboardStatsDTO.builder()
                .totalAliquotas(aliquotas.size())
                .ultimaVigencia(ultimaVigencia)
                .totalRegimes(regimes)
                .receitaSimulada(receitaSimulada)
                .cargaTributariaMedia(cargaMedia)
                .aliquotasPorRegime(porRegime)
                .distribuicaoTributos(distribuicao)
                .build();
    }
}
