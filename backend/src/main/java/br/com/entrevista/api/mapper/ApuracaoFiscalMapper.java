package br.com.entrevista.api.mapper;

import br.com.entrevista.api.dto.ApuracaoFiscalDTO;
import br.com.entrevista.domain.entity.ApuracaoFiscal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApuracaoFiscalMapper {

    ApuracaoFiscalDTO toDto(ApuracaoFiscal entity);

    ApuracaoFiscal toEntity(ApuracaoFiscalDTO dto);
}
