package com.Samuel.event_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventFullException extends RuntimeException {

    public EventFullException() {
        super("O evento já está lotado.");
    }

    public EventFullException(String message) {
        super(message);
    }
}
