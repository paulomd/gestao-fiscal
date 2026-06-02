package br.com.entrevista.api.controller;

import br.com.entrevista.api.dto.ApuracaoFiscalDTO;
import br.com.entrevista.api.dto.ApuracaoRequestDTO;
import br.com.entrevista.service.ApuracaoFiscalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apuracoes")
@RequiredArgsConstructor
@Tag(name = "Apurações Fiscais")
@SecurityRequirement(name = "bearer-jwt")
public class ApuracaoFiscalController {

    private final ApuracaoFiscalService service;

    @GetMapping
    public List<ApuracaoFiscalDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ApuracaoFiscalDTO buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ApuracaoFiscalDTO> salvar(@Valid @RequestBody ApuracaoFiscalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }

    @PostMapping("/calcular")
    public ResponseEntity<ApuracaoFiscalDTO> calcular(
            @Valid @RequestBody ApuracaoRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        String usuario = jwt.getClaimAsString("preferred_username");
        if (usuario == null) {
            usuario = jwt.getSubject();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.calcularESalvar(request, usuario));
    }
}
