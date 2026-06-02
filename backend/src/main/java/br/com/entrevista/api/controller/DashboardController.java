package br.com.entrevista.api.controller;

import br.com.entrevista.api.dto.DashboardStatsDTO;
import br.com.entrevista.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
@SecurityRequirement(name = "bearer-jwt")
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/stats")
    public DashboardStatsDTO stats() {
        return service.obterEstatisticas();
    }
}
