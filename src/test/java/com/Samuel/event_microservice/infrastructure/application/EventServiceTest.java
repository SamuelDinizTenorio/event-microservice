package com.Samuel.event_microservice.infrastructure.application;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.EventStatus;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.infrastructure.adapters.EventNotificationAdapter;
import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import com.Samuel.event_microservice.core.exceptions.EventFullException;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepositoryPort eventRepository;

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private EventNotificationAdapter eventNotificationAdapter;

    // Método auxiliar para criar uma entidade Event
    private Event createEventEntity(String title, LocalDateTime start, LocalDateTime end, int maxParticipants) {
        return new Event(
                title,
                "Descrição",
                start,
                end,
                maxParticipants,
                "http://image.url",
                "http://event.url",
                null,
                true);
    }

    @Test
    @DisplayName("Should create an event successfully when given valid data")
    void createEvent_withValidData_shouldReturnCreatedEventResponseDTO() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusHours(2);

        EventRequestDTO eventDTO = new EventRequestDTO(
                "Evento Válido",
                "Descrição",
                start,
                end,
                100,
                "http://image.url",
                "http://event.url",
                null,
                true
        );

        Event newEvent = createEventEntity("Evento Válido", start, end, 100);
        
        when(eventRepository.save(any(Event.class))).thenReturn(newEvent);

        // Act
        EventResponseDTO createdEventResponse = eventService.createEvent(eventDTO);

        // Assert
        assertNotNull(createdEventResponse);
        assertEquals(eventDTO.title(), createdEventResponse.title());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should return a PageResponseDTO of all events")
    void getAllEvents_shouldReturnPageResponseDTOOfEvents() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event event = createEventEntity("Evento Qualquer", start, start.plusHours(1), 100);
        Page<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findAll(pageable)).thenReturn(eventPage);

        // Act
        PageResponseDTO<EventResponseDTO> resultPage = eventService.getAllEvents(pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.totalElements());
        assertEquals(1, resultPage.content().size());
        assertEquals("Evento Qualquer", resultPage.content().getFirst().title());
        verify(eventRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return a PageResponseDTO of upcoming events by calling the correct repository method")
    void getUpcomingEvents_shouldCallRepositoryAndMapResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        Event upcomingEvent = createEventEntity("Evento Futuro", start, start.plusHours(2), 100);
        Page<Event> upcomingEventsPage = new PageImpl<>(List.of(upcomingEvent), pageable, 1);

        when(eventRepository.findUpcomingEvents(any(LocalDateTime.class), eq(pageable))).thenReturn(upcomingEventsPage);

        // Act
        PageResponseDTO<EventResponseDTO> resultPage = eventService.getUpcomingEvents(pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.totalElements());
        assertEquals(1, resultPage.content().size());
        assertEquals("Evento Futuro", resultPage.content().getFirst().title());
        verify(eventRepository, times(1)).findUpcomingEvents(any(LocalDateTime.class), eq(pageable));
    }

    @Test
    @DisplayName("Should return event details when a valid ID is provided")
    void getEventDetails_withValidId_shouldReturnEventDetails() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event event = createEventEntity("Evento Detalhado", start, start.plusHours(3), 100);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        EventResponseDTO resultDTO = eventService.getEventDetails(eventId);

        // Assert
        assertNotNull(resultDTO);
        assertEquals(event.getTitle(), resultDTO.title());
        verify(eventRepository, times(1)).findById(eventId);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when an invalid ID is provided")
    void getEventDetails_withInvalidId_shouldThrowEventNotFoundException() {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();

        when(eventRepository.findById(invalidEventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EventNotFoundException.class, () -> eventService.getEventDetails(invalidEventId));
        verify(eventRepository, times(1)).findById(invalidEventId);
    }

    @Test
    @DisplayName("Should register participant successfully and send confirmation email")
    void registerParticipant_withValidData_shouldSucceedAndNotify() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event event = createEventEntity("Evento para Registro", start, start.plusHours(1), 10);
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com")).thenReturn(Optional.empty());
        doNothing().when(eventNotificationAdapter).sendRegistrationConfirmation(any(Event.class), anyString());

        // Act
        eventService.registerParticipant(eventId, subscriptionDTO);

        // Assert
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(eventRepository, times(1)).save(event);
        verify(eventNotificationAdapter, times(1)).sendRegistrationConfirmation(event, "test@example.com");
        assertEquals(1, event.getRegisteredParticipants());
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when trying to register for a non-existent event")
    void registerParticipant_withInvalidEventId_shouldThrowEventNotFoundException() {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        when(eventRepository.findById(invalidEventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EventNotFoundException.class, () -> eventService.registerParticipant(invalidEventId, subscriptionDTO));
    }

    @Test
    @DisplayName("Should throw SubscriptionAlreadyExistsException when participant is already registered")
    void registerParticipant_whenAlreadyRegistered_shouldThrowSubscriptionAlreadyExistsException() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Event event = createEventEntity("Evento com Duplicado", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1);
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");
        Subscription existingSubscription = new Subscription(event, "test@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com")).thenReturn(Optional.of(existingSubscription));

        // Act & Assert
        assertThrows(SubscriptionAlreadyExistsException.class, () -> eventService.registerParticipant(eventId, subscriptionDTO));
    }

    @Test
    @DisplayName("Should throw EventFullException when trying to register for a full event")
    void registerParticipant_whenEventIsFull_shouldThrowEventFullException() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event fullEvent = createEventEntity("Evento Lotado", start, start.plusHours(1), 1);
        fullEvent.registerParticipant(); // Lota o evento (registeredParticipants = 1)

        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("new@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(fullEvent));
        when(subscriptionRepository.findByEventAndParticipantEmail(fullEvent, "new@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EventFullException.class, () -> eventService.registerParticipant(eventId, subscriptionDTO));
    }

    @Test
    @DisplayName("Should complete registration even if notification fails")
    void registerParticipant_whenNotificationFails_shouldStillRegister() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event event = createEventEntity("Evento com Falha de Email", start, start.plusHours(2), 10);
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com")).thenReturn(Optional.empty());
        
        // Simula falha no serviço de notificação
        doThrow(new RuntimeException("Email service is down")).when(eventNotificationAdapter).sendRegistrationConfirmation(any(Event.class), anyString());

        // Act
        eventService.registerParticipant(eventId, subscriptionDTO);

        // Assert
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(eventRepository, times(1)).save(event);
        verify(eventNotificationAdapter, times(1)).sendRegistrationConfirmation(event, "test@example.com");
    }

    @Test
    @DisplayName("Should return a PageResponseDTO of registered participants for a valid event")
    void getRegisteredParticipants_withValidEventId_shouldReturnParticipantsPageResponseDTO() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Event event = createEventEntity("Evento com Participantes", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 100);
        Subscription subscription = new Subscription(event, "test@example.com");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription), pageable, 1);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(eventRepository.getReferenceById(eventId)).thenReturn(event);
        when(subscriptionRepository.findByEvent(event, pageable)).thenReturn(subscriptionPage);

        // Act
        PageResponseDTO<RegisteredParticipantDTO> resultPage = eventService.getRegisteredParticipants(eventId, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.totalElements());
        assertEquals(1, resultPage.content().size());
        assertEquals("test@example.com", resultPage.content().getFirst().participantEmail());
        verify(subscriptionRepository, times(1)).findByEvent(event, pageable);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when fetching participants for an invalid event")
    void getRegisteredParticipants_withInvalidEventId_shouldThrowEventNotFoundException() {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(eventRepository.existsById(invalidEventId)).thenReturn(false);

        // Act & Assert
        assertThrows(EventNotFoundException.class, () -> eventService.getRegisteredParticipants(invalidEventId, pageable));
        verify(subscriptionRepository, never()).findByEvent(any(), any());
    }

    @Test
    @DisplayName("Should cancel an event and notify participants")
    void cancelEvent_withValidEvent_shouldCancelAndNotify() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event event = createEventEntity("Evento para Cancelar", start, start.plusHours(1), 10);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        doNothing().when(eventNotificationAdapter).notifyParticipantsOfCancellation(any(Event.class));

        // Act
        eventService.cancelEvent(eventId);

        // Assert
        assertEquals(EventStatus.CANCELLED, event.getStatus());
        verify(eventRepository, times(1)).save(event);
        verify(eventNotificationAdapter, times(1)).notifyParticipantsOfCancellation(event);
    }

    @Test
    @DisplayName("Should throw exception when trying to register for an inactive event")
    void registerParticipant_forInactiveEvent_shouldThrowException() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event cancelledEvent = createEventEntity("Evento Cancelado", start, start.plusHours(1), 10);
        cancelledEvent.cancel(); // Cancela o evento
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(cancelledEvent));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.registerParticipant(eventId, subscriptionDTO);
        });
    }
}
