package br.com.entrevista.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "apuracao_fiscal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApuracaoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 7)
    private String competencia;

    @Column(name = "receita_bruta", nullable = false, precision = 15, scale = 2)
    private BigDecimal receitaBruta;

    @Column(name = "regime_tributario", nullable = false, length = 50)
    private String regimeTributario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal pis;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cofins;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal irpj;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal csll;

    @Column(name = "total_tributos", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalTributos;

    @Column(name = "carga_tributaria", nullable = false, precision = 8, scale = 4)
    private BigDecimal cargaTributaria;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "data_calculo", nullable = false)
    private LocalDateTime dataCalculo;

    @PrePersist
    void prePersist() {
        if (dataCalculo == null) {
            dataCalculo = LocalDateTime.now();
        }
    }
}
