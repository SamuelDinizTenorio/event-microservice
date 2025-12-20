package com.Samuel.event_microservice.core.models;

import com.Samuel.event_microservice.core.exceptions.EventFullException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    // Método auxiliar para criar um Evento válido
    private Event createValidEvent() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusHours(2);
        return new Event(
                "Evento de Teste",
                "Descrição do evento.",
                start,
                end,
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
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusHours(2);
        int maxParticipants = 100;

        // Act
        Event event = new Event(title, description, start, end, maxParticipants, "http://image.url", "http://event.url", null, true);

        // Assert
        assertNotNull(event);
        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertEquals(start, event.getStartDateTime());
        assertEquals(end, event.getEndDateTime());
        assertEquals(maxParticipants, event.getMaxParticipants());
        assertEquals(0, event.getRegisteredParticipants());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when start date is in the past")
    void constructor_shouldThrowException_whenStartDateIsInThePast() {
        // Arrange
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = pastDate.plusHours(1);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Event("Evento de Teste", "Descrição", pastDate, endDate, 100, "http://image.url", "http://event.url", null, true));

        assertEquals("A data de início do evento não pode ser no passado.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when end date is before start date")
    void constructor_shouldThrowException_whenEndDateIsBeforeStartDate() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = startDate.minusMinutes(1); // Data de fim antes do início

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Event("Evento de Teste", "Descrição", startDate, endDate, 100, "http://image.url", "http://event.url", null, true));

        assertEquals("A data de encerramento deve ser posterior à data de início.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when event duration is less than minimum")
    void constructor_shouldThrowException_whenDurationIsTooShort() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = startDate.plusMinutes(10); // Duração menor que 15 minutos

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Event("Evento de Teste", "Descrição", startDate, endDate, 100, "http://image.url", "http://event.url", null, true));

        assertEquals("O evento deve ter uma duração de pelo menos 15 minutos.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when max participants is zero")
    void constructor_shouldThrowException_whenMaxParticipantsIsZero() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        int zeroParticipants = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Event("Evento de Teste", "Descrição", start, start.plusHours(1), zeroParticipants, "http://image.url", "http://event.url", null, true));

        assertEquals("O número máximo de participantes deve ser maior que 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Should register a participant successfully and increment counter")
    void registerParticipant_shouldIncrementCounter_whenEventIsNotFull() {
        // Arrange
        Event event = createValidEvent();
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
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event event = new Event("Evento Pequeno", "Desc", start, start.plusHours(1), 1, "img", "url", null, true);
        event.registerParticipant(); // Lota o evento

        // Act & Assert
        assertThrows(EventFullException.class, event::registerParticipant);
    }

    @Test
    @DisplayName("Should correctly test equals and hashCode contracts for JPA entities")
    void testEqualsAndHashCodeContracts() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Event newEvent1 = new Event("Title", "Desc", start, end, 100, "img", "url", "loc", false);
        Event newEvent2 = new Event("Title", "Desc", start, end, 100, "img", "url", "loc", false);

        Event persistedEvent1 = Event.builder().id(id1).title("Title A").build();
        Event samePersistedEvent1 = Event.builder().id(id1).title("Title B").build();

        Event differentPersistedEvent = Event.builder().id(id2).title("Title A").build();

        // --- Assert para equals() ---
        assertNotEquals(newEvent1, newEvent2, "New entities with null IDs should not be equal.");
        assertEquals(newEvent1, newEvent1, "An entity should be equal to itself.");
        assertEquals(persistedEvent1, samePersistedEvent1, "Entities with the same ID should be equal.");
        assertNotEquals(persistedEvent1, differentPersistedEvent, "Entities with different IDs should not be equal.");
        assertNotEquals(null, persistedEvent1);

        // --- Assert para hashCode() ---
        assertEquals(newEvent1.hashCode(), persistedEvent1.hashCode(), "HashCode should be constant across entity lifecycle.");
        assertEquals(persistedEvent1.hashCode(), samePersistedEvent1.hashCode(), "Equal objects must have equal hashCodes.");
    }
}
