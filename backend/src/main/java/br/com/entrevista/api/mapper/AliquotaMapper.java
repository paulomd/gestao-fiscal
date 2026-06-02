package br.com.entrevista.api.mapper;

import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.domain.entity.Aliquota;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AliquotaMapper {

    AliquotaDTO toDto(Aliquota entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCadastro", ignore = true)
    Aliquota toEntity(AliquotaDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCadastro", ignore = true)
    void updateEntity(@MappingTarget Aliquota entity, AliquotaDTO dto);
}
