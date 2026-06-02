package br.com.entrevista.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "aliquota")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aliquota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "regime_tributario", nullable = false, length = 50)
    private String regimeTributario;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal pis;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal cofins;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal irpj;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal csll;

    @Column(nullable = false)
    private LocalDate vigencia;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    void prePersist() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }
}
