package br.com.entrevista.jsf.converter;

import jakarta.faces.convert.ConverterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDateConverterTest {

    private LocalDateConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LocalDateConverter();
    }

    @Test
    void getAsObject_parseDdMmYyyy() {
        LocalDate data = converter.getAsObject(null, null, "15/03/2025");
        assertThat(data).isEqualTo(LocalDate.of(2025, 3, 15));
    }

    @Test
    void getAsObject_parseIso() {
        LocalDate data = converter.getAsObject(null, null, "2025-03-15");
        assertThat(data).isEqualTo(LocalDate.of(2025, 3, 15));
    }

    @Test
    void getAsObject_vazioRetornaNull() {
        assertThat(converter.getAsObject(null, null, null)).isNull();
        assertThat(converter.getAsObject(null, null, "   ")).isNull();
    }

    @Test
    void getAsObject_dataInvalida_lancaConverterException() {
        assertThatThrownBy(() -> converter.getAsObject(null, null, "32/13/2025"))
                .isInstanceOf(ConverterException.class)
                .hasMessageContaining("Data inválida");
    }

    @Test
    void getAsString_formataDdMmYyyy() {
        assertThat(converter.getAsString(null, null, LocalDate.of(2025, 1, 1)))
                .isEqualTo("01/01/2025");
    }

    @Test
    void getAsString_nullRetornaVazio() {
        assertThat(converter.getAsString(null, null, null)).isEmpty();
    }
}
