package com.Samuel.event_microservice.infrastructure.scheduler;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.EventStatus;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventStatusUpdaterServiceTest {

    @InjectMocks
    private EventStatusUpdaterService eventStatusUpdaterService;

    @Mock
    private EventRepositoryPort eventRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    /**
     * Configura o mock do TransactionTemplate para executar a lógica da transação.
     */
    private void arrangeTransactionTemplate() {
        when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(null);
                });
    }

    private Event createActiveEvent(String title) {
        var start = LocalDateTime.now().minusHours(2);
        return Event.builder()
                .id(UUID.randomUUID())
                .title(title)
                .startDateTime(start)
                .endDateTime(start.plusHours(1))
                .status(EventStatus.ACTIVE)
                .build();
    }

    /**
     * Método auxiliar que configura os mocks para o cenário onde dois eventos são encontrados.
     * @return A lista de eventos que foi configurada para ser retornada pelo repositório.
     */
    private List<Event> arrangeTwoEventsFound() {
        Event event1 = createActiveEvent("Event 1");
        Event event2 = createActiveEvent("Event 2");
        List<Event> finishedEvents = List.of(event1, event2);

        when(eventRepository.findActiveEventsFinishedBefore(any(LocalDateTime.class)))
                .thenReturn(finishedEvents);
        
        when(eventRepository.findById(event1.getId()))
                .thenReturn(Optional.of(event1));
        when(eventRepository.findById(event2.getId()))
                .thenReturn(Optional.of(event2));

        return finishedEvents;
    }

    @Nested
    @DisplayName("Tests for updateFinishedEvents method")
    class UpdateFinishedEvents {

        @Test
        @DisplayName("Should update status of finished events to FINISHED")
        void shouldUpdateStatusAndSave_whenEventsAreFound() {
            // Arrange
            arrangeTransactionTemplate();
            List<Event> finishedEvents = arrangeTwoEventsFound();
            Event event1 = finishedEvents.get(0);
            Event event2 = finishedEvents.get(1);

            // Act
            eventStatusUpdaterService.updateFinishedEvents();

            // Assert
            assertThat(event1.getStatus()).isEqualTo(EventStatus.FINISHED);
            assertThat(event2.getStatus()).isEqualTo(EventStatus.FINISHED);
            verify(eventRepository, times(2)).save(any(Event.class));
        }

        @Test
        @DisplayName("Should do nothing when no finished events are found")
        void shouldDoNothing_whenNoEventsAreFound() {
            // Arrange
            when(eventRepository.findActiveEventsFinishedBefore(any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // Act
            eventStatusUpdaterService.updateFinishedEvents();

            // Assert
            verify(eventRepository, never()).save(any(Event.class));
            verify(transactionTemplate, never()).execute(any());
        }

        @Test
        @DisplayName("Should continue updating other events if one save fails")
        void shouldContinue_whenOneSaveFails() {
            // Arrange
            arrangeTransactionTemplate();
            List<Event> finishedEvents = arrangeTwoEventsFound();
            Event eventToFail = finishedEvents.get(0);
            Event eventToSucceed = finishedEvents.get(1);

            doThrow(new RuntimeException("Database error"))
                    .when(eventRepository).save(eventToFail);

            when(eventRepository.save(eventToSucceed))
                    .thenReturn(eventToSucceed);

            // Act
            eventStatusUpdaterService.updateFinishedEvents();

            // Assert
            assertThat(eventToFail.getStatus()).isEqualTo(EventStatus.FINISHED);
            assertThat(eventToSucceed.getStatus()).isEqualTo(EventStatus.FINISHED);

            verify(eventRepository, times(1)).save(eventToFail);
            verify(eventRepository, times(1)).save(eventToSucceed);
        }
    }
}
