package br.com.entrevista.jsf.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@FacesConverter(value = "localDateConverter", forClass = LocalDate.class)
public class LocalDateConverter implements Converter<LocalDate> {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public LocalDate getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String texto = value.trim();
        try {
            if (texto.contains("/")) {
                return LocalDate.parse(texto, DISPLAY);
            }
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new ConverterException("Data inválida — use dd/MM/aaaa ou AAAA-MM-DD", e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalDate value) {
        return value == null ? "" : value.format(DISPLAY);
    }
}
