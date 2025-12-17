package com.Samuel.event_microservice.infrastructure.dto.subscription;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para encapsular os dados de requisição para a inscrição de um participante em um evento.
 *
 * @param participantEmail O endereço de e-mail do participante. Não pode estar em branco e deve ser um formato de e-mail válido.
 */
public record SubscriptionRequestDTO(
        @NotBlank(message = "O e-mail do participante não pode estar em branco.")
        @Email(message = "Formato de e-mail inválido.")
        String participantEmail
) {
}
