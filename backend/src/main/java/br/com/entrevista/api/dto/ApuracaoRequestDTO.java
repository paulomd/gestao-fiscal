package br.com.entrevista.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApuracaoRequestDTO {

    @NotBlank
    @Pattern(regexp = "\\d{2}/\\d{4}")
    private String competencia;

    @NotNull @DecimalMin("0.01")
    private BigDecimal receitaBruta;

    @NotBlank
    private String regimeTributario;
}
