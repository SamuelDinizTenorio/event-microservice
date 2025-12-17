package com.Samuel.event_microservice.core.models;

import com.Samuel.event_microservice.core.exceptions.EventFullException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    // Método auxiliar para criar um Evento usando o construtor de tipos primitivos
    private Event createValidEvent() {
        return new Event(
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
        String title = "Evento de Teste";
        String description = "Descrição do evento.";
        LocalDateTime date = LocalDateTime.now().plusDays(10);
        int maxParticipants = 100;
        String imageUrl = "http://image.url";
        String eventUrl = "http://event.url";
        String location = null;
        Boolean isRemote = true;

        // Act
        Event event = new Event(title, description, date, maxParticipants, imageUrl, eventUrl, location, isRemote);

        // Assert
        assertNotNull(event);
        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertEquals(date, event.getDate());
        assertEquals(maxParticipants, event.getMaxParticipants());
        assertEquals(0, event.getRegisteredParticipants()); // Deve começar com 0
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when event date is in the past")
    void constructor_shouldThrowException_whenDateIsInThePast() {
        // Arrange
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Event("Evento de Teste", "Descrição", pastDate, 100, "http://image.url", "http://event.url", null, true);
        });

        assertEquals("A data do evento não pode ser no passado.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when max participants is zero")
    void constructor_shouldThrowException_whenMaxParticipantsIsZero() {
        // Arrange
        int zeroParticipants = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Event("Evento de Teste", "Descrição", LocalDateTime.now().plusDays(1), zeroParticipants, "http://image.url", "http://event.url", null, true);
        });

        assertEquals("O número máximo de participantes deve ser maior que 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Should register a participant successfully and increment counter")
    void registerParticipant_shouldIncrementCounter_whenEventIsNotFull() {
        // Arrange
        Event event = createValidEvent(); // Usando o método auxiliar
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
        Event event = new Event("Evento Pequeno", "Desc", LocalDateTime.now().plusDays(1), 1, "img", "url", null, true);
        event.registerParticipant(); // Lota o evento

        // Act & Assert
        assertThrows(EventFullException.class, event::registerParticipant);
    }
}
