package com.Samuel.event_microservice.core.data;

import java.time.LocalDateTime;

/**
 * Objeto de dados imutável que carrega as informações para a atualização de um evento.
 * <p>
 * Este record vive na camada de domínio (core) e é usado pela entidade Event para
 * receber os dados de atualização, mantendo o domínio desacoplado da camada de infraestrutura (DTOs).
 *
 * @param title O novo título do evento.
 * @param description A nova descrição do evento.
 * @param startDateTime A nova data e hora de início do evento.
 * @param endDateTime A nova data e hora de encerramento do evento.
 * @param maxParticipants O novo número máximo de participantes.
 * @param imageUrl A nova URL da imagem do evento.
 * @param eventUrl A nova URL do evento (se remoto).
 * @param location A nova localização do evento (se presencial).
 * @param remote O novo status de evento remoto.
 */
public record EventUpdateData(
        String title,
        String description,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer maxParticipants,
        String imageUrl,
        String eventUrl,
        String location,
        Boolean remote
) {
}
