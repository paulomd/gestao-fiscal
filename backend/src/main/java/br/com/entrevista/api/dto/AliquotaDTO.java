package br.com.entrevista.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliquotaDTO {

    private Long id;

    @NotBlank(message = "Regime tributário é obrigatório")
    private String regimeTributario;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private BigDecimal pis;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private BigDecimal cofins;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private BigDecimal irpj;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private BigDecimal csll;

    @NotNull
    private LocalDate vigencia;

    private LocalDateTime dataCadastro;
}
