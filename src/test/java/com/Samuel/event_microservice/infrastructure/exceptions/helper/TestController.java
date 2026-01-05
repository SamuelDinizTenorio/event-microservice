package com.Samuel.event_microservice.infrastructure.exceptions.helper;

import com.Samuel.event_microservice.core.exceptions.EventFullException;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Um controller "dummy" que existe apenas para lançar exceções
 * e ser usado em testes que validam o GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/test") // Usando um caminho base para todos os endpoints de teste
public class TestController {

    @PostMapping(value = "/validation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void testValidation(@Valid @RequestBody ValidationTestDTO dto) {
        // Atingido apenas se a validação passar. Usado para testar 400, 405 e 415.
    }

    @GetMapping("/event-not-found")
    public void throwEventNotFound() {
        throw new EventNotFoundException("Evento de teste não encontrado.");
    }

    @GetMapping("/event-full")
    public void throwEventFull() {
        throw new EventFullException("Evento de teste está lotado.");
    }

    @GetMapping("/subscription-exists")
    public void throwSubscriptionExists() {
        throw new SubscriptionAlreadyExistsException("Inscrição de teste já existe.");
    }

    @GetMapping("/illegal-argument")
    public void throwIllegalArgument() {
        throw new IllegalArgumentException("Argumento de teste inválido.");
    }

    @GetMapping("/illegal-state")
    public void throwIllegalStateException() {
        throw new IllegalStateException("O estado do recurso é inválido para esta operação.");
    }

    @PostMapping("/message-not-readable")
    public void throwMessageNotReadableException(@RequestBody String body) {
        // A exceção é lançada pelo Spring antes se o corpo estiver ausente.
    }

    @GetMapping("/unhandled-exception")
    public void throwUnhandledException() {
        throw new RuntimeException("Erro genérico e inesperado.");
    }
}