package com.Samuel.event_microservice.infrastructure.application;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO; // Novo import
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import com.Samuel.event_microservice.core.exceptions.EventFullException;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.core.ports.EmailSender;
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
    private EmailSender emailSender;

    // Método auxiliar para criar uma entidade Event
    private Event createEventEntity(String title, LocalDateTime date, int maxParticipants) {
        return new Event(title, "Descrição", date, maxParticipants, "http://image.url", "http://event.url", null, true);
    }

    // Método auxiliar para criar um EventRequestDTO
    private EventRequestDTO createEventRequestDTO(String title, LocalDateTime date, int maxParticipants) {
        return new EventRequestDTO(title, "Descrição", date, maxParticipants, "http://image.url", "http://event.url", null, true);
    }

    @Test
    @DisplayName("Should create an event successfully when given valid data")
    void createEvent_withValidData_shouldReturnCreatedEventResponseDTO() {
        // Arrange
        EventRequestDTO eventDTO = createEventRequestDTO("Evento Válido", LocalDateTime.now().plusDays(10), 100);
        Event newEvent = createEventEntity("Evento Válido", LocalDateTime.now().plusDays(10), 100);
        
        // Mocka o repositório para retornar o evento salvo
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
        Event event = createEventEntity("Evento Qualquer", LocalDateTime.now(), 100);
        Page<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findAll(pageable)).thenReturn(eventPage);

        // Act
        PageResponseDTO<EventResponseDTO> resultPage = eventService.getAllEvents(pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.totalElements());
        assertEquals(1, resultPage.content().size());
        assertEquals("Evento Qualquer", resultPage.content().get(0).title());
        verify(eventRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return a PageResponseDTO of upcoming events by calling the correct repository method")
    void getUpcomingEvents_shouldCallRepositoryAndMapResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Event upcomingEvent = createEventEntity("Evento Futuro", LocalDateTime.now().plusDays(5), 100);
        Page<Event> upcomingEventsPage = new PageImpl<>(List.of(upcomingEvent), pageable, 1);

        when(eventRepository.findUpcomingEvents(any(LocalDateTime.class), eq(pageable))).thenReturn(upcomingEventsPage);

        // Act
        PageResponseDTO<EventResponseDTO> resultPage = eventService.getUpcomingEvents(pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.totalElements());
        assertEquals(1, resultPage.content().size());
        assertEquals("Evento Futuro", resultPage.content().get(0).title());
        
        verify(eventRepository, times(1)).findUpcomingEvents(any(LocalDateTime.class), eq(pageable));
        verify(eventRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should return event details when a valid ID is provided")
    void getEventDetails_withValidId_shouldReturnEventDetails() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Event event = createEventEntity("Evento Detalhado", LocalDateTime.now().plusDays(1), 100);
        
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
    @DisplayName("Should register participant successfully when all conditions are met")
    void registerParticipant_withValidData_shouldSucceed() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Event event = createEventEntity("Evento para Registro", LocalDateTime.now().plusDays(1), 10);
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com")).thenReturn(Optional.empty());

        // Act
        eventService.registerParticipant(eventId, subscriptionDTO);

        // Assert
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(eventRepository, times(1)).save(event);
        verify(emailSender, times(1)).sendEmail(any());
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
        Event event = createEventEntity("Evento com Duplicado", LocalDateTime.now().plusDays(1), 10);
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
        EventRequestDTO dto = createEventRequestDTO("Evento Lotado", LocalDateTime.now().plusDays(1), 1);
        Event fullEvent = createEventEntity("Evento Lotado", LocalDateTime.now().plusDays(1), 1);
        fullEvent.registerParticipant(); // Lota o evento (registeredParticipants = 1)

        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("new@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(fullEvent));
        when(subscriptionRepository.findByEventAndParticipantEmail(fullEvent, "new@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EventFullException.class, () -> eventService.registerParticipant(eventId, subscriptionDTO));
    }

    @Test
    @DisplayName("Should complete registration even if email sending fails")
    void registerParticipant_whenEmailFails_shouldStillRegister() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Event event = createEventEntity("Evento com Falha de Email", LocalDateTime.now().plusDays(1), 10);
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Email service is down")).when(emailSender).sendEmail(any());

        // Act
        eventService.registerParticipant(eventId, subscriptionDTO);

        // Assert
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(eventRepository, times(1)).save(event);
        verify(emailSender, times(1)).sendEmail(any());
    }

    @Test
    @DisplayName("Should return a PageResponseDTO of registered participants for a valid event")
    void getRegisteredParticipants_withValidEventId_shouldReturnParticipantsPageResponseDTO() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        Event event = createEventEntity("Evento com Participantes", LocalDateTime.now().plusDays(1), 100);
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
        assertEquals("test@example.com", resultPage.content().get(0).participantEmail());
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
}
