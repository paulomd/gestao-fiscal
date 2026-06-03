package br.com.entrevista.service;

import br.com.entrevista.api.dto.ApuracaoRequestDTO;
import br.com.entrevista.api.dto.DashboardStatsDTO;
import br.com.entrevista.config.TestSecurityConfig;
import br.com.entrevista.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AliquotaService aliquotaService;

    @Autowired
    private ApuracaoFiscalService apuracaoService;

    @Test
    void obterEstatisticas_semDados_usaDefaults() {
        DashboardStatsDTO stats = dashboardService.obterEstatisticas();

        assertThat(stats.getTotalAliquotas()).isZero();
        assertThat(stats.getUltimaVigencia()).isEqualTo("-");
        assertThat(stats.getReceitaSimulada()).isEqualByComparingTo("100000");
        assertThat(stats.getCargaTributariaMedia()).isEqualByComparingTo("33.25");
        assertThat(stats.getDistribuicaoTributos()).isEmpty();
    }

    @Test
    void obterEstatisticas_comDados_agregaCorretamente() {
        aliquotaService.criar(TestDataFactory.aliquotaLucroPresumido());
        aliquotaService.criar(TestDataFactory.aliquotaLucroReal());
        apuracaoService.calcularESalvar(
                new ApuracaoRequestDTO("03/2025", new BigDecimal("200000"), "Lucro Presumido"),
                "fiscal");

        DashboardStatsDTO stats = dashboardService.obterEstatisticas();

        assertThat(stats.getTotalAliquotas()).isEqualTo(2);
        assertThat(stats.getTotalRegimes()).isEqualTo(2);
        assertThat(stats.getUltimaVigencia()).isEqualTo("01/06/2025");
        assertThat(stats.getReceitaSimulada()).isEqualByComparingTo("200000");
        assertThat(stats.getAliquotasPorRegime()).hasSize(2);
        assertThat(stats.getDistribuicaoTributos()).containsKeys("PIS", "COFINS", "IRPJ", "CSLL");
    }
}
