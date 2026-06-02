package br.com.entrevista.service;

import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.api.mapper.AliquotaMapper;
import br.com.entrevista.domain.entity.Aliquota;
import br.com.entrevista.domain.repository.AliquotaRepository;
import br.com.entrevista.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AliquotaService {

    private final AliquotaRepository repository;
    private final AliquotaMapper mapper;

    @Transactional(readOnly = true)
    public List<AliquotaDTO> listar(String filtro) {
        List<Aliquota> entities = filtro == null || filtro.isBlank()
                ? repository.findAll()
                : repository.findByRegimeTributarioContainingIgnoreCase(filtro);
        return entities.stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AliquotaDTO buscarPorId(Long id) {
        return mapper.toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public AliquotaDTO buscarPorRegime(String regime) {
        return repository.findFirstByRegimeTributarioOrderByVigenciaDesc(regime)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Alíquota não encontrada para regime: " + regime));
    }

    @Transactional
    public AliquotaDTO criar(AliquotaDTO dto) {
        Aliquota entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    public AliquotaDTO atualizar(Long id, AliquotaDTO dto) {
        Aliquota entity = findEntity(id);
        mapper.updateEntity(entity, dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Alíquota não encontrada: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long contar() {
        return repository.count();
    }

    private Aliquota findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alíquota não encontrada: " + id));
    }
}
