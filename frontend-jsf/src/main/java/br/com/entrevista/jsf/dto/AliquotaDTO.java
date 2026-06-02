package br.com.entrevista.jsf.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AliquotaDTO {
    private Long id;
    private String regimeTributario;
    private BigDecimal pis;
    private BigDecimal cofins;
    private BigDecimal irpj;
    private BigDecimal csll;
    private LocalDate vigencia;
    private LocalDateTime dataCadastro;
}
