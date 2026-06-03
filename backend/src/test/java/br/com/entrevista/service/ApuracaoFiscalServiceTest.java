package br.com.entrevista.service;

import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.api.dto.ApuracaoFiscalDTO;
import br.com.entrevista.api.dto.ApuracaoRequestDTO;
import br.com.entrevista.config.TestSecurityConfig;
import br.com.entrevista.exception.ResourceNotFoundException;
import br.com.entrevista.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class ApuracaoFiscalServiceTest {

    @Autowired
    private ApuracaoFiscalService apuracaoService;

    @Autowired
    private AliquotaService aliquotaService;

    @BeforeEach
    void cadastrarAliquotaLucroPresumido() {
        aliquotaService.criar(TestDataFactory.aliquotaLucroPresumido());
    }

    @Test
    void calcularESalvar_lucroPresumido_aplicaPercentuaisDaAliquota() {
        var request = new ApuracaoRequestDTO(
                "01/2025",
                new BigDecimal("500000.00"),
                "Lucro Presumido");

        ApuracaoFiscalDTO result = apuracaoService.calcularESalvar(request, "fiscal");

        // tributo = receita × (% alíquota / 100)
        assertThat(result.getPis()).isEqualByComparingTo("3250.00");
        assertThat(result.getCofins()).isEqualByComparingTo("15000.00");
        assertThat(result.getIrpj()).isEqualByComparingTo("24000.00");
        assertThat(result.getCsll()).isEqualByComparingTo("14400.00");
        assertThat(result.getTotalTributos()).isEqualByComparingTo("56650.00");
        assertThat(result.getCargaTributaria()).isEqualByComparingTo("11.3300");
        assertThat(result.getUsuario()).isEqualTo("fiscal");
    }

    @Test
    void listarBuscarESalvarApuracao() {
        var calculada = apuracaoService.calcularESalvar(
                new ApuracaoRequestDTO("02/2025", new BigDecimal("100000"), "Lucro Presumido"),
                "admin");

        assertThat(apuracaoService.listar()).hasSize(1);
        assertThat(apuracaoService.buscarPorId(calculada.getId()).getCompetencia()).isEqualTo("02/2025");

        calculada.setReceitaBruta(new BigDecimal("120000"));
        ApuracaoFiscalDTO salva = apuracaoService.salvar(calculada);
        assertThat(salva.getReceitaBruta()).isEqualByComparingTo("120000");
    }

    @Test
    void buscarApuracaoInexistente_lancaNotFound() {
        assertThatThrownBy(() -> apuracaoService.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
