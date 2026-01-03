package com.Samuel.event_microservice.core.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class SubscriptionTest {

    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Cria um evento de teste que pode ser reutilizado em vários testes
        this.testEvent = Event.builder()
                .title("Evento de Teste")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Nested
    @DisplayName("Tests for constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should create subscription successfully when data is valid")
        void shouldCreateSubscription_whenDataIsValid() {
            // Arrange
            String email = "test@example.com";

            // Act
            Subscription subscription = new Subscription(testEvent, email);

            // Assert
            assertThat(subscription).isNotNull().satisfies(sub -> {
                assertThat(sub.getId()).isNull();
                assertThat(sub.getEvent()).isEqualTo(testEvent);
                assertThat(sub.getParticipantEmail()).isEqualTo(email);
                assertThat(sub.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when event is null")
        void shouldThrowException_whenEventIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new Subscription(null, "test@example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O evento não pode ser nulo.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when participant email is null")
        void shouldThrowException_whenEmailIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> new Subscription(testEvent, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O email do participante não pode ser nulo ou vazio.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when participant email is blank")
        void shouldThrowException_whenEmailIsBlank() {
            // Act & Assert
            assertThatThrownBy(() -> new Subscription(testEvent, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O email do participante não pode ser nulo ou vazio.");
        }
    }

    @Nested
    @DisplayName("Tests for equals and hashCode contracts")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should follow equals and hashCode contracts for JPA entities")
        void shouldFollowContracts() {
            // Arrange
            Subscription newSub1 = new Subscription(testEvent, "user1@test.com");
            Subscription newSub2 = new Subscription(testEvent, "user1@test.com");

            // Usando o Builder para criar objetos "mock-persistidos"
            Subscription persistedSub1 = Subscription.builder().id(1L).event(testEvent).participantEmail("user2@test.com").build();
            Subscription sameAsPersistedSub1 = Subscription.builder().id(1L).event(testEvent).participantEmail("user3@test.com").build();
            Subscription persistedSub2 = Subscription.builder().id(2L).event(testEvent).participantEmail("user2@test.com").build();

            // --- Contrato de Equals ---
            assertThat(persistedSub1).isNotEqualTo(null);
            assertThat(newSub1).isNotEqualTo(newSub2); // Duas entidades novas nunca são iguais
            assertThat(persistedSub1).isEqualTo(sameAsPersistedSub1); // Entidades com mesmo ID são iguais
            assertThat(persistedSub1).isNotEqualTo(persistedSub2); // Entidades com IDs diferentes não são iguais

            // --- Contrato de HashCode ---
            // Se dois objetos são 'equals', eles DEVEM ter o mesmo hashCode
            assertThat(persistedSub1.hashCode()).isEqualTo(sameAsPersistedSub1.hashCode());
            // Com a implementação de hashCode constante, todos os hashCodes serão iguais
            assertThat(newSub1.hashCode()).isEqualTo(persistedSub1.hashCode());
        }
    }
}
