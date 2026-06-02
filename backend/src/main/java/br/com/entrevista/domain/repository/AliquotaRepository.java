package br.com.entrevista.domain.repository;

import br.com.entrevista.domain.entity.Aliquota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AliquotaRepository extends JpaRepository<Aliquota, Long>, JpaSpecificationExecutor<Aliquota> {

    Optional<Aliquota> findFirstByRegimeTributarioOrderByVigenciaDesc(String regimeTributario);

    List<Aliquota> findByRegimeTributarioContainingIgnoreCase(String regime);
}
