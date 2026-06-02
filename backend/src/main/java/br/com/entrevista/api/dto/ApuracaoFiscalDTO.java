package br.com.entrevista.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApuracaoFiscalDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = "\\d{2}/\\d{4}", message = "Competência deve estar no formato MM/yyyy")
    private String competencia;

    @NotNull @DecimalMin("0.01")
    private BigDecimal receitaBruta;

    private String regimeTributario;

    private BigDecimal pis;
    private BigDecimal cofins;
    private BigDecimal irpj;
    private BigDecimal csll;
    private BigDecimal totalTributos;
    private BigDecimal cargaTributaria;
    private String usuario;
    private LocalDateTime dataCalculo;
}
