package com.Samuel.event_microservice.infrastructure.dto.event;

import com.Samuel.event_microservice.core.models.Event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para encapsular os dados de resposta de um evento.
 * Usado para retornar informações detalhadas de um evento para o cliente.
 *
 * @param id                   O identificador único do evento.
 * @param title                O título do evento.
 * @param description          A descrição detalhada do evento.
 * @param date                 A data e hora em que o evento ocorrerá.
 * @param maxParticipants      O número máximo de participantes permitidos.
 * @param registeredParticipants O número de participantes atualmente registrados.
 * @param imageUrl             A URL de uma imagem de banner para o evento.
 * @param eventUrl             A URL para acessar o evento, caso seja remoto.
 * @param location             O endereço físico do evento, caso seja presencial.
 * @param isRemote             Indica se o evento é remoto (online) ou não.
 */
public record EventResponseDTO(
        UUID id,
        String title,
        String description,
        LocalDateTime date,
        int maxParticipants,
        int registeredParticipants,
        String imageUrl,
        String eventUrl,
        String location,
        Boolean isRemote
) {
    /**
     * Construtor que cria um EventResponseDTO a partir de uma entidade Event.
     *
     * @param event A entidade Event da qual os dados serão copiados.
     */
    public EventResponseDTO(Event event) {
        this(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getMaxParticipants(),
                event.getRegisteredParticipants(),
                event.getImageUrl(),
                event.getEventUrl(),
                event.getLocation(),
                event.getIsRemote()
        );
    }
}
