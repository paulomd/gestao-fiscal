package br.com.entrevista.service;

import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.api.mapper.AliquotaMapper;
import br.com.entrevista.domain.entity.Aliquota;
import br.com.entrevista.domain.repository.AliquotaRepository;
import br.com.entrevista.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import br.com.entrevista.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class AliquotaServiceTest {

    @Autowired
    private AliquotaService service;

    @Autowired
    private AliquotaRepository repository;

    @Autowired
    private AliquotaMapper mapper;

    @Test
    void deveCriarEBuscarAliquota() {
        AliquotaDTO dto = AliquotaDTO.builder()
                .regimeTributario("Lucro Real")
                .pis(BigDecimal.valueOf(1.65))
                .cofins(BigDecimal.valueOf(7.6))
                .irpj(BigDecimal.valueOf(15))
                .csll(BigDecimal.valueOf(9))
                .vigencia(LocalDate.now())
                .build();

        AliquotaDTO criada = service.criar(dto);
        assertThat(criada.getId()).isNotNull();

        AliquotaDTO encontrada = service.buscarPorId(criada.getId());
        assertThat(encontrada.getRegimeTributario()).isEqualTo("Lucro Real");
    }

    @Test
    void deveLancarExcecaoQuandoNaoEncontrada() {
        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
