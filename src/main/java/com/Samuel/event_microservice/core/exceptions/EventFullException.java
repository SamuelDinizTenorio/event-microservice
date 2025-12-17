package com.Samuel.event_microservice.core.exceptions;

import com.Samuel.event_microservice.infrastructure.exceptions.GlobalExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma tentativa de registro é feita em um evento que já atingiu
 * sua capacidade máxima de participantes.
 * <p>
 * Esta exceção resulta em uma resposta HTTP 400 (Bad Request) quando tratada pelo
 * {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EventFullException extends RuntimeException {

    public EventFullException() {
        super("O evento já está lotado.");
    }

    public EventFullException(String message) {
        super(message);
    }
}
