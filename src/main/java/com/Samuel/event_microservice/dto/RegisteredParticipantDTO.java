package com.Samuel.event_microservice.dto;

/**
 * DTO para representar um participante registrado em um evento.
 * Atualmente, contém apenas o e-mail do participante.
 *
 * @param participantEmail O endereço de e-mail do participante.
 */
public record RegisteredParticipantDTO(
        String participantEmail
) {
}
