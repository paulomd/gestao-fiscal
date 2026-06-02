package br.com.entrevista.domain.repository;

import br.com.entrevista.domain.entity.ApuracaoFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApuracaoFiscalRepository extends JpaRepository<ApuracaoFiscal, Long>, JpaSpecificationExecutor<ApuracaoFiscal> {
}
