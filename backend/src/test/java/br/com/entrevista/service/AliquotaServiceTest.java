package br.com.entrevista.service;

import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.config.TestSecurityConfig;
import br.com.entrevista.exception.ResourceNotFoundException;
import br.com.entrevista.support.TestDataFactory;
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
class AliquotaServiceTest {

    @Autowired
    private AliquotaService service;

    @Test
    void deveCriarEBuscarAliquota() {
        AliquotaDTO criada = service.criar(TestDataFactory.aliquotaLucroReal());
        assertThat(criada.getId()).isNotNull();

        AliquotaDTO encontrada = service.buscarPorId(criada.getId());
        assertThat(encontrada.getRegimeTributario()).isEqualTo("Lucro Real");
    }

    @Test
    void deveListarFiltrarAtualizarExcluirEContar() {
        AliquotaDTO presumido = service.criar(TestDataFactory.aliquotaLucroPresumido());
        service.criar(TestDataFactory.aliquotaLucroReal());

        assertThat(service.listar(null)).hasSize(2);
        assertThat(service.listar("presumido")).hasSize(1);
        assertThat(service.buscarPorRegime("Lucro Real").getPis()).isEqualByComparingTo("1.65");

        presumido.setPis(new BigDecimal("1.00"));
        assertThat(service.atualizar(presumido.getId(), presumido).getPis()).isEqualByComparingTo("1.00");

        service.excluir(presumido.getId());
        assertThat(service.contar()).isEqualTo(1);
        assertThatThrownBy(() -> service.buscarPorId(presumido.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoQuandoNaoEncontrada() {
        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.buscarPorRegime("Inexistente"))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.excluir(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
