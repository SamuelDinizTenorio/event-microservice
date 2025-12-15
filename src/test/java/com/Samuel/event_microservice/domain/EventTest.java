package com.Samuel.event_microservice.domain;

import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.exceptions.EventFullException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    // Método auxiliar para criar um DTO válido
    private EventRequestDTO createValidDTO() {
        return new EventRequestDTO(
                "Evento de Teste",
                "Descrição do evento.",
                LocalDateTime.now().plusDays(10), // Data futura
                100,
                "http://image.url",
                "http://event.url",
                null,
                true
        );
    }

    @Test
    @DisplayName("Should create event successfully when data is valid")
    void constructor_shouldCreateEvent_whenDataIsValid() {
        // Arrange
        EventRequestDTO dto = createValidDTO();

        // Act
        Event event = new Event(dto);

        // Assert
        assertNotNull(event);
        assertEquals(dto.title(), event.getTitle());
        assertEquals(dto.description(), event.getDescription());
        assertEquals(dto.date(), event.getDate());
        assertEquals(dto.maxParticipants(), event.getMaxParticipants());
        assertEquals(0, event.getRegisteredParticipants()); // Deve começar com 0
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when event date is in the past")
    void constructor_shouldThrowException_whenDateIsInThePast() {
        // Arrange
        EventRequestDTO dtoWithPastDate = new EventRequestDTO(
                "Evento de Teste",
                "Descrição",
                LocalDateTime.now().minusDays(1), // Data no passado
                100,
                "http://image.url",
                "http://event.url",
                null,
                true
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Event(dtoWithPastDate);
        });

        assertEquals("A data do evento não pode ser no passado.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when max participants is zero")
    void constructor_shouldThrowException_whenMaxParticipantsIsZero() {
        // Arrange
        EventRequestDTO dtoWithZeroParticipants = new EventRequestDTO(
                "Evento de Teste",
                "Descrição",
                LocalDateTime.now().plusDays(1),
                0, // Participantes inválido
                "http://image.url",
                "http://event.url",
                null,
                true
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Event(dtoWithZeroParticipants);
        });

        assertEquals("O número máximo de participantes deve ser maior que 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Should register a participant successfully and increment counter")
    void registerParticipant_shouldIncrementCounter_whenEventIsNotFull() {
        // Arrange
        Event event = new Event(createValidDTO());
        assertEquals(0, event.getRegisteredParticipants());

        // Act
        event.registerParticipant();

        // Assert
        assertEquals(1, event.getRegisteredParticipants());
    }

    @Test
    @DisplayName("Should throw EventFullException when trying to register in a full event")
    void registerParticipant_shouldThrowException_whenEventIsFull() {
        // Arrange
        EventRequestDTO dto = new EventRequestDTO(
                "Evento Pequeno", "Desc", LocalDateTime.now().plusDays(1), 1, "img", "url", null, true
        );
        Event event = new Event(dto);
        event.registerParticipant(); // Lota o evento

        // Act & Assert
        assertThrows(EventFullException.class, event::registerParticipant);
    }
}
