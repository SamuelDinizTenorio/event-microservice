package com.Samuel.event_microservice.domain;

import com.Samuel.event_microservice.dto.EventRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionTest {

    private Event createTestEvent() {
        EventRequestDTO dto = new EventRequestDTO(
                "Evento de Teste",
                "Descrição do evento.",
                LocalDateTime.now().plusDays(10),
                100,
                "http://image.url",
                "http://event.url",
                null,
                true
        );
        return new Event(dto);
    }

    @Test
    @DisplayName("Should create subscription successfully when data is valid")
    void constructor_shouldCreateSubscription_whenDataIsValid() {
        // Arrange
        Event event = createTestEvent();
        String email = "test@example.com";

        // Act
        Subscription subscription = new Subscription(event, email);

        // Assert
        assertNotNull(subscription);
        assertEquals(event, subscription.getEvent());
        assertEquals(email, subscription.getParticipantEmail());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when event is null")
    void constructor_shouldThrowException_whenEventIsNull() {
        // Arrange
        String email = "test@example.com";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Subscription(null, email);
        });

        assertEquals("O evento não pode ser nulo.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when participant email is null")
    void constructor_shouldThrowException_whenEmailIsNull() {
        // Arrange
        Event event = createTestEvent();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Subscription(event, null);
        });

        assertEquals("O email do participante não pode ser nulo ou vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when participant email is blank")
    void constructor_shouldThrowException_whenEmailIsBlank() {
        // Arrange
        Event event = createTestEvent();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Subscription(event, "   "); // Email em branco
        });

        assertEquals("O email do participante não pode ser nulo ou vazio.", exception.getMessage());
    }
}
