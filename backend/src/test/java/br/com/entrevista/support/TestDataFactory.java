package br.com.entrevista.support;

import br.com.entrevista.api.dto.AliquotaDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static AliquotaDTO aliquotaLucroPresumido() {
        return AliquotaDTO.builder()
                .regimeTributario("Lucro Presumido")
                .pis(new BigDecimal("0.65"))
                .cofins(new BigDecimal("3.00"))
                .irpj(new BigDecimal("4.80"))
                .csll(new BigDecimal("2.88"))
                .vigencia(LocalDate.of(2025, 1, 1))
                .build();
    }

    public static AliquotaDTO aliquotaLucroReal() {
        return AliquotaDTO.builder()
                .regimeTributario("Lucro Real")
                .pis(new BigDecimal("1.65"))
                .cofins(new BigDecimal("7.60"))
                .irpj(new BigDecimal("15.00"))
                .csll(new BigDecimal("9.00"))
                .vigencia(LocalDate.of(2025, 6, 1))
                .build();
    }
}
