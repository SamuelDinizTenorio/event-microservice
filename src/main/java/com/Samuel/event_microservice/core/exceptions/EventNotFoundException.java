package com.Samuel.event_microservice.core.exceptions;

import com.Samuel.event_microservice.infrastructure.exceptions.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma operação tenta acessar um evento que não existe
 * no banco de dados.
 * <p>
 * Esta exceção resulta em uma resposta HTTP 404 (Not Found) quando tratada pelo
 * {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException() {
        super("Evento não encontrado.");
    }

    public EventNotFoundException(String message) {
        super(message);
    }
}
