package br.com.entrevista.service;

import br.com.entrevista.api.dto.ApuracaoFiscalDTO;
import br.com.entrevista.api.dto.ApuracaoRequestDTO;
import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.api.mapper.ApuracaoFiscalMapper;
import br.com.entrevista.domain.entity.ApuracaoFiscal;
import br.com.entrevista.domain.repository.ApuracaoFiscalRepository;
import br.com.entrevista.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApuracaoFiscalService {

    private final ApuracaoFiscalRepository repository;
    private final ApuracaoFiscalMapper mapper;
    private final AliquotaService aliquotaService;

    @Transactional(readOnly = true)
    public List<ApuracaoFiscalDTO> listar() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApuracaoFiscalDTO buscarPorId(Long id) {
        return mapper.toDto(findEntity(id));
    }

    @Transactional
    public ApuracaoFiscalDTO calcularESalvar(ApuracaoRequestDTO request, String usuario) {
        AliquotaDTO aliquota = aliquotaService.buscarPorRegime(request.getRegimeTributario());
        BigDecimal receita = request.getReceitaBruta();

        BigDecimal pis = calcularValor(receita, aliquota.getPis());
        BigDecimal cofins = calcularValor(receita, aliquota.getCofins());
        BigDecimal irpj = calcularValor(receita, aliquota.getIrpj());
        BigDecimal csll = calcularValor(receita, aliquota.getCsll());
        BigDecimal total = pis.add(cofins).add(irpj).add(csll);
        BigDecimal carga = total.multiply(BigDecimal.valueOf(100))
                .divide(receita, 4, RoundingMode.HALF_UP);

        ApuracaoFiscal entity = ApuracaoFiscal.builder()
                .competencia(request.getCompetencia())
                .receitaBruta(receita)
                .regimeTributario(request.getRegimeTributario())
                .pis(pis)
                .cofins(cofins)
                .irpj(irpj)
                .csll(csll)
                .totalTributos(total)
                .cargaTributaria(carga)
                .usuario(usuario)
                .build();

        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    public ApuracaoFiscalDTO salvar(ApuracaoFiscalDTO dto) {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    private BigDecimal calcularValor(BigDecimal receita, BigDecimal percentual) {
        return receita.multiply(percentual).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private ApuracaoFiscal findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apuração não encontrada: " + id));
    }
}
