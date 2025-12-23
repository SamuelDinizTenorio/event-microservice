package com.Samuel.event_microservice.infrastructure.scheduler;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.EventStatus;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventStatusUpdaterServiceTest {

    @InjectMocks
    private EventStatusUpdaterService eventStatusUpdaterService;

    @Mock
    private EventRepositoryPort eventRepository;

    private Event createActiveEvent(String title) {
        var start = LocalDateTime.now().minusHours(2);
        return Event.builder()
                .title(title)
                .startDateTime(start)
                .endDateTime(start.plusHours(1))
                .status(EventStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should update status of finished events to FINISHED")
    void updateFinishedEvents_whenEventsAreFound_shouldUpdateStatusAndSave() {
        // Arrange
        Event event1 = createActiveEvent("Event 1");
        Event event2 = createActiveEvent("Event 2");
        List<Event> finishedEvents = List.of(event1, event2);

        when(eventRepository.findActiveEventsFinishedBefore(any(LocalDateTime.class))).thenReturn(finishedEvents);

        // Act
        eventStatusUpdaterService.updateFinishedEvents();

        // Assert
        // Verifica se o status de cada evento foi alterado para FINISHED
        assertEquals(EventStatus.FINISHED, event1.getStatus());
        assertEquals(EventStatus.FINISHED, event2.getStatus());

        // Verifica se o método save foi chamado para cada evento
        verify(eventRepository, times(2)).save(any(Event.class));
        verify(eventRepository).save(event1);
        verify(eventRepository).save(event2);
    }

    @Test
    @DisplayName("Should do nothing when no finished events are found")
    void updateFinishedEvents_whenNoEventsAreFound_shouldDoNothing() {
        // Arrange
        when(eventRepository.findActiveEventsFinishedBefore(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // Act
        eventStatusUpdaterService.updateFinishedEvents();

        // Assert
        // Garante que nenhuma operação de salvamento foi tentada
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Should continue updating other events if one fails")
    void updateFinishedEvents_whenOneSaveFails_shouldContinue() {
        // Arrange
        Event event1 = createActiveEvent("Event 1");
        Event event2 = createActiveEvent("Event 2");
        List<Event> finishedEvents = List.of(event1, event2);

        when(eventRepository.findActiveEventsFinishedBefore(any(LocalDateTime.class))).thenReturn(finishedEvents);
        // Simula uma falha ao salvar o primeiro evento
        doThrow(new RuntimeException("Database error")).when(eventRepository).save(event1);

        // Act
        eventStatusUpdaterService.updateFinishedEvents();

        // Assert
        // O status do primeiro evento foi alterado, mesmo que o save tenha falhado
        assertEquals(EventStatus.FINISHED, event1.getStatus());
        // O status do segundo evento também foi alterado
        assertEquals(EventStatus.FINISHED, event2.getStatus());

        // Verifica que o save foi tentado para ambos os eventos
        verify(eventRepository, times(1)).save(event1);
        verify(eventRepository, times(1)).save(event2);
    }
}
