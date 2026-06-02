package br.com.entrevista.api.controller;

import br.com.entrevista.api.dto.AliquotaDTO;
import br.com.entrevista.service.AliquotaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aliquotas")
@RequiredArgsConstructor
@Tag(name = "Alíquotas")
@SecurityRequirement(name = "bearer-jwt")
public class AliquotaController {

    private final AliquotaService service;

    @GetMapping
    public List<AliquotaDTO> listar(@RequestParam(required = false) String filtro) {
        return service.listar(filtro);
    }

    @GetMapping("/{id}")
    public AliquotaDTO buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/regime/{regime}")
    public AliquotaDTO buscarPorRegime(@PathVariable String regime) {
        return service.buscarPorRegime(regime);
    }

    @PostMapping
    public ResponseEntity<AliquotaDTO> criar(@Valid @RequestBody AliquotaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public AliquotaDTO atualizar(@PathVariable Long id, @Valid @RequestBody AliquotaDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
