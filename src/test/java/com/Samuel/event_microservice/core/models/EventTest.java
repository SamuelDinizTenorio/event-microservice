package com.Samuel.event_microservice.core.models;

import com.Samuel.event_microservice.core.data.EventUpdateData;
import com.Samuel.event_microservice.core.exceptions.EventFullException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class EventTest {

    private static final int MIN_DURATION = 15;

    /**
     * Método auxiliar para criar um evento de teste com um status específico.
     * @param status O status desejado para o evento (ex: ACTIVE, CANCELLED).
     * @return Uma instância de Event com dados de teste.
     */
    private Event createEventWithStatus(EventStatus status) {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        return Event.builder()
                .title("Evento de Teste")
                .description("Descrição de teste")
                .startDateTime(start)
                .endDateTime(start.plusHours(2))
                .maxParticipants(100)
                .isRemote(true)
                .eventUrl("http://event.url")
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("Tests for constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should create event successfully when data is valid")
        void shouldCreateEvent_whenDataIsValid() {
            // Arrange
            String title = "Evento de Teste";
            String description = "Descrição do evento.";
            LocalDateTime start = LocalDateTime.now().plusDays(10);
            LocalDateTime end = start.plusHours(2);
            int maxParticipants = 100;

            // Act
            Event event = new Event(title, description, start, end, maxParticipants, "http://image.url", "http://event.url", null, true, MIN_DURATION);

            // Assert
            assertThat(event)
                    .isNotNull()
                    .satisfies(e -> {
                        assertThat(e.getTitle()).isEqualTo(title);
                        assertThat(e.getDescription()).isEqualTo(description);
                        assertThat(e.getStartDateTime()).isEqualTo(start);
                        assertThat(e.getEndDateTime()).isEqualTo(end);
                        assertThat(e.getMaxParticipants()).isEqualTo(maxParticipants);
                        assertThat(e.getRegisteredParticipants()).isZero();
                        assertThat(e.getStatus()).isEqualTo(EventStatus.ACTIVE);
                    });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when start date is in the past")
        void shouldThrowException_whenStartDateIsInThePast() {
            // Arrange
            LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = pastDate.plusHours(1);

            // Act & Assert
            assertThatThrownBy(() -> new Event("Evento de Teste", "Descrição longa", pastDate, endDate, 100, "http://image.url", "http://event.url", null, true, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A data de início do evento deve ser no futuro.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when end date is before start date")
        void shouldThrowException_whenEndDateIsBeforeStartDate() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = startDate.minusMinutes(1); // Data de fim antes do início

            // Act & Assert
            assertThatThrownBy(() -> new Event("Evento de Teste", "Descrição longa", startDate, endDate, 100, "http://image.url", "http://event.url", null, true, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A data de encerramento deve ser posterior à data de início.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when event duration is less than minimum")
        void shouldThrowException_whenDurationIsTooShort() {
            // Arrange
            LocalDateTime startDate = LocalDateTime.now().plusDays(1);
            LocalDateTime endDate = startDate.plusMinutes(MIN_DURATION - 1); // Duração menor que a mínima

            // Act & Assert
            assertThatThrownBy(() -> new Event("Evento de Teste", "Descrição longa", startDate, endDate, 100, "http://image.url", "http://event.url", null, true, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O evento deve ter uma duração de pelo menos " + MIN_DURATION + " minutos.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when max participants is zero")
        void shouldThrowException_whenMaxParticipantsIsZero() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            int zeroParticipants = 0;

            // Act & Assert
            assertThatThrownBy(() -> new Event("Evento de Teste", "Descrição longa", start, start.plusHours(1), zeroParticipants, "http://image.url", "http://event.url", null, true, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O número máximo de participantes deve ser maior que 0.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for is_remote event without URL")
        void shouldThrowException_whenRemoteEventHasNoUrl() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);

            // Act & Assert
            // Tenta criar um evento remoto (is_remote=true) mas com eventUrl nulo
            assertThatThrownBy(() -> new Event("Remote Event", "Descrição longa", start, start.plusHours(1), 100, "img.url", null, null, true, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Um evento remoto deve ter uma URL de evento.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for non-is_remote event without location")
        void shouldThrowException_whenNonRemoteEventHasNoLocation() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);

            // Act & Assert
            // Tenta criar um evento presencial (is_remote=false) mas com location nula
            assertThatThrownBy(() -> new Event("Local Event", "Descrição longa", start, start.plusHours(1), 100, "img.url", null, null, false, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Um evento presencial deve ter uma localização física.");
        }
    }

    @Nested
    @DisplayName("Tests for registerParticipant method")
    class RegisterParticipantTest {

        @Test
        @DisplayName("Should register a participant successfully and increment counter")
        void shouldIncrementCounter_whenEventIsNotFull() {
            // Arrange
            Event event = createEventWithStatus(EventStatus.ACTIVE);
            assertThat(event.getRegisteredParticipants()).isZero();

            // Act
            event.registerParticipant();

            // Assert
            assertThat(event.getRegisteredParticipants()).isOne();
        }

        @Test
        @DisplayName("Should throw EventFullException when trying to register in a full event")
        void shouldThrowException_whenEventIsFull() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            Event fullEvent = Event.builder()
                    .title("Evento Lotado")
                    .description("Desc")
                    .startDateTime(start)
                    .endDateTime(start.plusHours(1))
                    .maxParticipants(100)
                    .registeredParticipants(100)
                    .status(EventStatus.ACTIVE)
                    .build();

            // Act & Assert
            assertThatThrownBy(fullEvent::registerParticipant)
                    .isInstanceOf(EventFullException.class)
                    .hasMessage("O evento já está lotado.");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when registering for an inactive event")
        void shouldThrowException_whenEventIsNotActive() {
            // Arrange
            Event cancelledEvent = createEventWithStatus(EventStatus.CANCELLED);

            // Act & Assert
            assertThatThrownBy(cancelledEvent::registerParticipant)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Não é possível se inscrever em um evento que não está ativo.");
        }
    }

    @Nested
    @DisplayName("Tests for cancel method")
    class CancelTest {

        @Test
        @DisplayName("Should change status to CANCELLED when cancelling an active event")
        void shouldChangeStatus_whenEventIsActive() {
            // Arrange
            Event event = createEventWithStatus(EventStatus.ACTIVE);

            // Act
            event.cancel();

            // Assert
            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when cancelling an already finished event")
        void shouldThrowException_whenEventIsFinished() {
            // Arrange
            var pastStart = LocalDateTime.now().minusHours(2);
            Event finishedEvent = Event.builder()
                    .startDateTime(pastStart)
                    .endDateTime(pastStart.plusHours(1))
                    .status(EventStatus.FINISHED)
                    .build();

            // Act & Assert
            assertThatThrownBy(finishedEvent::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Não é possível cancelar um evento que já ocorreu.");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when cancelling an already cancelled event")
        void shouldThrowException_whenEventIsAlreadyCancelled() {
            // Arrange
            Event cancelledEvent = createEventWithStatus(EventStatus.CANCELLED);

            // Act & Assert
            assertThatThrownBy(cancelledEvent::cancel)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Este evento já está cancelado.");
        }
    }

    @Nested
    @DisplayName("Tests for finish method")
    class FinishTest {

        @Test
        @DisplayName("Should change status to FINISHED when finishing an active, past event")
        void shouldChangeStatus_whenEventIsActiveAndPast() {
            // Arrange
            var start = LocalDateTime.now().minusHours(2);
            Event finishedEvent = Event.builder()
                    .startDateTime(start)
                    .endDateTime(start.plusHours(1))
                    .status(EventStatus.ACTIVE)
                    .build();

            // Act
            finishedEvent.finish();

            // Assert
            assertThat(finishedEvent.getStatus()).isEqualTo(EventStatus.FINISHED);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when finishing an event that is not active")
        void shouldThrowException_whenEventIsNotActive() {
            // Arrange
            Event cancelledEvent = createEventWithStatus(EventStatus.CANCELLED);

            // Act & Assert
            assertThatThrownBy(cancelledEvent::finish)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Apenas eventos ativos podem ser finalizados.");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when finishing an event that has not ended")
        void shouldThrowException_whenEventHasNotEnded() {
            // Arrange
            Event activeEvent = createEventWithStatus(EventStatus.ACTIVE);

            // Act & Assert
            assertThatThrownBy(activeEvent::finish)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Não é possível finalizar um evento que ainda não terminou.");
        }
    }

    @Nested
    @DisplayName("Tests for updateDetails method")
    class UpdateDetailsTests {

        @Test
        @DisplayName("Should update fields correctly when data object has valid data")
        void shouldUpdateFields_whenDataIsValid() {
            // Arrange
            // Cria um evento com todos os campos preenchidos para ter valores originais para comparar
            Event eventToUpdate = Event.builder()
                    .title("Título Original")
                    .description("Descrição Original")
                    .startDateTime(LocalDateTime.now().plusDays(5))
                    .endDateTime(LocalDateTime.now().plusDays(6))
                    .maxParticipants(50)
                    .registeredParticipants(5)
                    .imageUrl("http://original.image.url")
                    .eventUrl("http://original.event.url")
                    .location(null)
                    .isRemote(true)
                    .status(EventStatus.ACTIVE)
                    .build();

            // --- Salva o estado original ANTES da mutação ---
            String originalDescription = eventToUpdate.getDescription();
            String originalEventUrl = eventToUpdate.getEventUrl();
            String originalLocation = eventToUpdate.getLocation();
            boolean originalRemote = eventToUpdate.isRemote();
            int originalRegisteredParticipants = eventToUpdate.getRegisteredParticipants();
            EventStatus originalStatus = eventToUpdate.getStatus();

            // Novos valores para os campos que DEVEM mudar
            String newTitle = "Novo Título";
            LocalDateTime newStart = LocalDateTime.now().plusDays(20);
            LocalDateTime newEnd = newStart.plusHours(3);
            int newMaxParticipants = 200;
            String newImageUrl = "http://new.image.url";

            // EventUpdateData com alguns campos nulos (não devem mudar) e outros com novos valores
            EventUpdateData data = new EventUpdateData(
                    newTitle, null, newStart, newEnd, newMaxParticipants, newImageUrl,
                    null, null, null
            );

            // Act
            eventToUpdate.updateDetails(data, MIN_DURATION);

            // Assert
            assertThat(eventToUpdate).satisfies(updatedEvent -> {
                // --- Campos que DEVEM ter sido atualizados ---
                assertThat(updatedEvent.getTitle()).isEqualTo(newTitle);
                assertThat(updatedEvent.getStartDateTime()).isEqualTo(newStart);
                assertThat(updatedEvent.getEndDateTime()).isEqualTo(newEnd);
                assertThat(updatedEvent.getMaxParticipants()).isEqualTo(newMaxParticipants);
                assertThat(updatedEvent.getImageUrl()).isEqualTo(newImageUrl);

                // --- Campos que NÃO DEVEM ter sido alterados ---
                assertThat(updatedEvent.getDescription()).isEqualTo(originalDescription);
                assertThat(updatedEvent.getEventUrl()).isEqualTo(originalEventUrl);
                assertThat(updatedEvent.getLocation()).isEqualTo(originalLocation);
                assertThat(updatedEvent.isRemote()).isEqualTo(originalRemote);

                // --- Campos que nunca são alterados por updateDetails ---
                assertThat(updatedEvent.getRegisteredParticipants()).isEqualTo(originalRegisteredParticipants);
                assertThat(updatedEvent.getStatus()).isEqualTo(originalStatus);
            });
        }

        @Test
        @DisplayName("Should throw IllegalStateException when updating a non-active event")
        void shouldThrowException_whenEventIsNotActive() {
            // Arrange
            Event cancelledEvent = createEventWithStatus(EventStatus.CANCELLED);
            EventUpdateData data = new EventUpdateData("New Title", null, null, null, null, null, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> cancelledEvent.updateDetails(data, MIN_DURATION))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Apenas eventos ativos podem ser atualizados.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new maxParticipants is less than registered")
        void shouldThrowException_whenMaxParticipantsIsInvalid() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);

            Event event = Event.builder()
                    .title("Evento com Inscritos")
                    .startDateTime(start)
                    .endDateTime(start.plusHours(1))
                    .maxParticipants(5)
                    .registeredParticipants(2)
                    .status(EventStatus.ACTIVE)
                    .build();

            EventUpdateData invalidData = new EventUpdateData(null, null, null, null, 1, null, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> event.updateDetails(invalidData, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O número máximo de participantes não pode ser menor que o número de inscritos (" + event.getRegisteredParticipants() + ").");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new dates are inconsistent")
        void shouldThrowException_whenDatesAreInconsistent() {
            // Arrange
            Event validEvent = createEventWithStatus(EventStatus.ACTIVE);
            LocalDateTime newEnd = validEvent.getStartDateTime().minusDays(1);
            EventUpdateData data = new EventUpdateData(null, null, null, newEnd, null, null, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> validEvent.updateDetails(data, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A data de encerramento deve ser posterior à data de início.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when making a is_remote event have a location")
        void shouldThrowException_whenRemoteHasLocation() {
            // Arrange
            Event remoteEvent = createEventWithStatus(EventStatus.ACTIVE);
            EventUpdateData invalidData = new EventUpdateData(null, null, null, null, null, null, null, "Some Location", null);

            // Act & Assert
            assertThatThrownBy(() -> remoteEvent.updateDetails(invalidData, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Um evento remoto não pode ter uma localização física.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating to an inconsistent state (e.g., is_remote with location)")
        void shouldThrowException_whenUpdatingToInconsistentState() {
            // Arrange
            Event nonRemoteEvent = Event.builder()
                    .title("Evento Presencial")
                    .description("Descrição Longa")
                    .startDateTime(LocalDateTime.now().plusDays(1))
                    .endDateTime(LocalDateTime.now().plusDays(2))
                    .maxParticipants(100)
                    .location("Local Original")
                    .isRemote(false)
                    .status(EventStatus.ACTIVE)
                    .build();

            // Tenta atualizar o evento para ser remoto (is_remote=true), mas NÃO remove a localização existente.
            // A validação final em updateDetails() deve pegar essa inconsistência.
            EventUpdateData invalidData = new EventUpdateData(null, null, null, null, null, null, null, null, true);

            // Act & Assert
            assertThatThrownBy(() -> nonRemoteEvent.updateDetails(invalidData, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Um evento remoto não pode ter uma localização física.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating start date to the past")
        void shouldThrowException_whenUpdatingStartDateToPast() {
            // Arrange
            Event event = createEventWithStatus(EventStatus.ACTIVE);
            EventUpdateData dataWithPastDate = new EventUpdateData(null, null, LocalDateTime.now().minusDays(1), null, null, null, null, null, null);

            // Act & Assert
            assertThatThrownBy(() -> event.updateDetails(dataWithPastDate, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A data de início do evento deve ser no futuro.");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating max participants to zero or less")
        void shouldThrowException_whenUpdatingMaxParticipantsToInvalid() {
            // Arrange
            Event event = createEventWithStatus(EventStatus.ACTIVE);

            // Tenta atualizar a capacidade máxima para um valor inválido (zero)
            EventUpdateData dataWithInvalidCapacity = new EventUpdateData(
                    null, null, null, null, 0, null, null, null, null
            );

            // Act & Assert
            assertThatThrownBy(() -> event.updateDetails(dataWithInvalidCapacity, MIN_DURATION))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("O número máximo de participantes deve ser maior que 0.");
        }
    }

    @Nested
    @DisplayName("Tests for equals and hashCode")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should follow equals and hashCode contracts for JPA entities")
        void equalsAndHashCode_shouldFollowContracts() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();

            // Cenário 1: Duas entidades novas, ainda não persistidas (IDs nulos)
            Event newEvent1 = Event.builder().title("New Event").build();
            Event newEvent2 = Event.builder().title("New Event").build();

            // Cenário 2: Uma entidade "mock-persistida" e outra com o mesmo ID
            Event persistedEvent1 = Event.builder().id(id1).title("Title A").build();
            Event sameAsPersistedEvent1 = Event.builder().id(id1).title("Title B").build();

            // Cenário 3: Duas entidades "mock-persistidas" com IDs diferentes
            Event persistedEvent2 = Event.builder().id(id2).title("Title A").build();

            // --- Contrato de Equals ---

            // 1. Uma entidade nunca é igual a nulo
            assertThat(persistedEvent1).isNotEqualTo(null);

            // 2. Duas entidades novas (sem ID) nunca são iguais
            assertThat(newEvent1).isNotEqualTo(newEvent2);

            // 3. Entidades com o mesmo ID são consideradas iguais
            assertThat(persistedEvent1).isEqualTo(sameAsPersistedEvent1);

            // 4. Entidades com IDs diferentes nunca são iguais
            assertThat(persistedEvent1).isNotEqualTo(persistedEvent2);

            // --- Contrato de HashCode ---

            // 1. Se dois objetos são 'equals', eles DEVEM ter o mesmo hashCode
            assertThat(persistedEvent1.hashCode()).isEqualTo(sameAsPersistedEvent1.hashCode());

            // 2. (Opcional, mas bom) Se dois objetos NÃO são 'equals', eles PODEM ter hashCodes diferentes.
            //    Com a implementação de hashCode constante, eles terão o mesmo hashCode, o que é permitido.
            //    Esta asserção verifica se a implementação é a de hash constante.
            assertThat(newEvent1.hashCode()).isEqualTo(persistedEvent1.hashCode());
        }
    }
}
