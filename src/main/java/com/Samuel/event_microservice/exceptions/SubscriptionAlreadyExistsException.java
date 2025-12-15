package com.Samuel.event_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma tentativa de inscrição é feita para um participante
 * que já está registrado no evento.
 * <p>
 * Esta exceção resulta em uma resposta HTTP 409 (Conflict) quando tratada pelo
 * {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SubscriptionAlreadyExistsException extends RuntimeException {

    public SubscriptionAlreadyExistsException() {
        super("Este participante já está inscrito neste evento.");
    }

    public SubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
