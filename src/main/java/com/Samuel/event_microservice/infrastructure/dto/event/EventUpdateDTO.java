package com.Samuel.event_microservice.infrastructure.dto.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para atualização parcial de um evento.
 * <p>
 * Permite que campos sejam nulos, indicando que não devem ser alterados.
 * Se um campo for fornecido (não nulo), ele deve passar por validações de formato e conteúdo.
 */
public record EventUpdateDTO(
        @Size(min = 3, message = "O título deve ter no mínimo 3 caracteres.")
        String title,

        @Size(min = 10, message = "A descrição deve ter no mínimo 10 caracteres.")
        String description,

        @Future(message = "A data de início do evento deve ser no futuro.")
        LocalDateTime startDateTime,

        LocalDateTime endDateTime,

        @Min(value = 1, message = "O número máximo de participantes deve ser maior que 0.")
        Integer maxParticipants,

        @URL(message = "A URL da imagem é inválida.")
        String imageUrl,

        @URL(message = "A URL do evento é inválida.")
        String eventUrl,

        String location,

        Boolean is_remote
) {
}
