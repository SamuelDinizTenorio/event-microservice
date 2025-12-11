package com.Samuel.event_microservice.dto;

import com.Samuel.event_microservice.domain.Event;

import java.time.LocalDate;
import java.util.UUID;

public record EventResponseDTO(
        UUID id,
        int maxParticipants,
        int registeredParticipants,
        String title,
        LocalDate date,
        String description
) {
    public EventResponseDTO(Event event) {
        this(
                event.getId(),
                event.getMaxParticipants(),
                event.getRegisteredParticipants(),
                event.getTitle(),
                event.getDate(),
                event.getDescription()
        );
    }
}
