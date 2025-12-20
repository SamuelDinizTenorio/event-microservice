package com.Samuel.event_microservice.core.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionTest {

    // Método auxiliar para criar um Evento usando o construtor de tipos primitivos
    private Event createTestEvent() {
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Subscription(null, email));

        assertEquals("O evento não pode ser nulo.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when participant email is null")
    void constructor_shouldThrowException_whenEmailIsNull() {
        // Arrange
        Event event = createTestEvent();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Subscription(event, null));

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

    @Test
    @DisplayName("Should correctly test equals and hashCode contracts for JPA entities")
    void testEqualsAndHashCodeContracts() {
        // Arrange
        Event event = createTestEvent();

        Subscription newSub1 = new Subscription(event, "user1@test.com");
        Subscription newSub2 = new Subscription(event, "user1@test.com");

        Subscription persistedSub1 = new Subscription(event, "user2@test.com");
        ReflectionTestUtils.setField(persistedSub1, "id", 1L);

        Subscription samePersistedSub1 = new Subscription(event, "user3@test.com");
        ReflectionTestUtils.setField(samePersistedSub1, "id", 1L);

        Subscription differentPersistedSub = new Subscription(event, "user2@test.com");
        ReflectionTestUtils.setField(differentPersistedSub, "id", 2L);

        // --- Assert para equals() ---
        assertNotEquals(newSub1, newSub2, "New entities with null IDs should not be equal.");
        assertEquals(newSub1, newSub1, "An entity should be equal to itself.");
        assertEquals(persistedSub1, samePersistedSub1, "Entities with the same ID should be equal.");
        assertNotEquals(persistedSub1, differentPersistedSub, "Entities with different IDs should not be equal.");
        assertNotEquals(null, persistedSub1);

        // --- Assert para hashCode() ---
        assertEquals(newSub1.hashCode(), persistedSub1.hashCode(), "HashCode should be constant across entity lifecycle.");
        assertEquals(persistedSub1.hashCode(), samePersistedSub1.hashCode(), "Equal objects must have equal hashCodes.");
    }
}
