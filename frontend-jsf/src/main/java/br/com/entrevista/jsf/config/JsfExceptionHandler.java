package br.com.entrevista.jsf.config;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

@Slf4j
public class JsfExceptionHandler extends ExceptionHandlerWrapper {

    private final ExceptionHandler wrapped;

    public JsfExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> events = getUnhandledExceptionQueuedEvents().iterator();
        while (events.hasNext()) {
            ExceptionQueuedEvent event = events.next();
            ExceptionQueuedEventContext context = event.getContext();
            Throwable t = context.getException();
            log.error("Erro JSF", t);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
                            mensagemSegura(t)));
            events.remove();
        }
        getWrapped().handle();
    }

    private static String mensagemSegura(Throwable t) {
        if (t instanceof ConverterException) {
            return "Valor inválido em um dos campos — use ponto para decimais (ex.: 1.65)";
        }
        if (t instanceof ValidatorException ve && ve.getFacesMessage() != null) {
            return ve.getFacesMessage().getDetail() != null
                    ? ve.getFacesMessage().getDetail()
                    : ve.getFacesMessage().getSummary();
        }
        String msg = t.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = t.getClass().getSimpleName();
        }
        if (msg.contains("<html") || msg.contains("<!DOCTYPE")) {
            return "Falha na operação (resposta inválida — verifique login ou API)";
        }
        return msg.replace('<', ' ').replace('>', ' ').trim();
    }
}
