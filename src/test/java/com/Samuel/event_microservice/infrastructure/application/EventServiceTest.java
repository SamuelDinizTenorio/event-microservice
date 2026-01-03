package com.Samuel.event_microservice.infrastructure.application;

import com.Samuel.event_microservice.core.data.EventUpdateData;
import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.EventStatus;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.core.exceptions.EventFullException;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.core.ports.EventNotificationPort;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import com.Samuel.event_microservice.infrastructure.config.EventBusinessConfig;
import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventUpdateDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private EventNotificationPort eventNotificationPort;

    @Mock
    private EventBusinessConfig eventBusinessConfig;

    private Event createEventEntity(String title, LocalDateTime start, int maxParticipants) {
        return Event.builder()
                .id(UUID.randomUUID())
                .title(title)
                .description("Descrição")
                .startDateTime(start)
                .endDateTime(start.plusHours(2))
                .maxParticipants(maxParticipants)
                .status(EventStatus.ACTIVE)
                .isRemote(true)
                .eventUrl("http://event.url")
                .build();
    }

    @Nested
    @DisplayName("Tests for createEvent method")
    class CreateEventTests {

        @Test
        @DisplayName("Should create an event successfully")
        void shouldCreateEvent_whenValidData() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(10);
            EventRequestDTO eventDTO = new EventRequestDTO(
                    "Evento Válido",
                    "Descrição do Evento",
                    start,
                    start.plusHours(2),
                    100,
                    "http://image.url",
                    "http://event.url",
                    null,
                    true
            );
            ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

            when(eventBusinessConfig.getMinDurationMinutes())
                    .thenReturn(15);
            when(eventRepository.save(eventCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            EventResponseDTO result = eventService.createEvent(eventDTO);

            // Assert
            // 1. Verifica a entidade passada para o banco de dados
            Event savedEvent = eventCaptor.getValue();
            assertThat(savedEvent)
                    .isNotNull()
                    .satisfies(event -> {
                        assertThat(event.getTitle()).isEqualTo(eventDTO.title());
                        assertThat(event.getDescription()).isEqualTo(eventDTO.description());
                        assertThat(event.getStartDateTime()).isEqualTo(eventDTO.startDateTime());
                        assertThat(event.getEndDateTime()).isEqualTo(eventDTO.endDateTime());
                        assertThat(event.getMaxParticipants()).isEqualTo(eventDTO.maxParticipants());
                        assertThat(event.getRegisteredParticipants()).isZero();
                        assertThat(event.getImageUrl()).isEqualTo(eventDTO.imageUrl());
                        assertThat(event.getEventUrl()).isEqualTo(eventDTO.eventUrl());
                        assertThat(event.getLocation()).isEqualTo(eventDTO.location());
                        assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);
                    });

            // 2. Verifica o DTO de resposta retornado para o cliente
            assertThat(result)
                    .isNotNull()
                    .satisfies(dto -> {
                assertThat(dto.title()).isEqualTo(eventDTO.title());
                assertThat(dto.description()).isEqualTo(eventDTO.description());
                assertThat(dto.startDateTime()).isEqualTo(eventDTO.startDateTime());
                assertThat(dto.endDateTime()).isEqualTo(eventDTO.endDateTime());
                assertThat(dto.maxParticipants()).isEqualTo(eventDTO.maxParticipants());
                assertThat(dto.registeredParticipants()).isZero();
                assertThat(dto.imageUrl()).isEqualTo(eventDTO.imageUrl());
                assertThat(dto.eventUrl()).isEqualTo(eventDTO.eventUrl());
                assertThat(dto.location()).isEqualTo(eventDTO.location());
                assertThat(dto.status()).isEqualTo(EventStatus.ACTIVE);
            });
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException from entity constructor")
        void shouldPropagateException_whenEntityValidationFails() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().minusDays(1); // Data inválida
            EventRequestDTO invalidEventDTO = new EventRequestDTO("Evento Inválido", "Descrição longa", start, start.plusHours(1), 100, null, null, null, true);
            when(eventBusinessConfig.getMinDurationMinutes())
                    .thenReturn(15);

            // Act & Assert
            assertThatThrownBy(() -> eventService.createEvent(invalidEventDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A data de início do evento deve ser no futuro.");

            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Tests for getAllEvents method")
    class GetAllEventsTests {

        @Test
        @DisplayName("Should return a PageResponseDTO of all events")
        void shouldReturnPageResponseDTOOfEvents() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Event event = createEventEntity("Evento Qualquer", LocalDateTime.now().plusDays(1), 100);
            Page<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);

            when(eventRepository.findAll(pageable))
                    .thenReturn(eventPage);

            // Act
            PageResponseDTO<EventResponseDTO> resultPage = eventService.getAllEvents(pageable);

            // Assert
            verify(eventRepository, times(1)).findAll(pageable);
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.totalElements()).isEqualTo(1);
            assertThat(resultPage.content())
                    .hasSize(1)
                    .first()
                    .satisfies(dto -> {
                        assertThat(dto.id()).isEqualTo(event.getId());
                        assertThat(dto.title()).isEqualTo(event.getTitle());
                        assertThat(dto.startDateTime()).isEqualTo(event.getStartDateTime());
                        assertThat(dto.endDateTime()).isEqualTo(event.getEndDateTime());
                        assertThat(dto.maxParticipants()).isEqualTo(event.getMaxParticipants());
                        assertThat(dto.registeredParticipants()).isEqualTo(event.getRegisteredParticipants());
                        assertThat(dto.imageUrl()).isEqualTo(event.getImageUrl());
                        assertThat(dto.eventUrl()).isEqualTo(event.getEventUrl());
                        assertThat(dto.location()).isEqualTo(event.getLocation());
                        assertThat(dto.is_remote()).isEqualTo(event.isRemote());
                        assertThat(dto.status()).isEqualTo(event.getStatus());
                    });
        }

        @Test
        @DisplayName("Should return an empty page when no events are found")
        void shouldReturnEmptyPage_whenNoEventsFound() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Event> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(eventRepository.findAll(pageable))
                    .thenReturn(emptyPage);

            // Act
            PageResponseDTO<EventResponseDTO> resultPage = eventService.getAllEvents(pageable);

            // Assert
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.totalElements()).isZero();
            assertThat(resultPage.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for getUpcomingEvents method")
    class GetUpcomingEventsTests {
        
        @Test
        @DisplayName("Should return a PageResponseDTO of upcoming events")
        void shouldCallRepositoryAndMapResult() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Event upcomingEvent = createEventEntity("Evento Futuro", LocalDateTime.now().plusDays(5), 100);
            Page<Event> upcomingEventsPage = new PageImpl<>(List.of(upcomingEvent), pageable, 1);

            when(eventRepository.findUpcomingEvents(any(LocalDateTime.class), eq(pageable)))
                    .thenReturn(upcomingEventsPage);

            // Act
            PageResponseDTO<EventResponseDTO> resultPage = eventService.getUpcomingEvents(pageable);

            // Assert
            verify(eventRepository, times(1)).findUpcomingEvents(any(LocalDateTime.class), eq(pageable));
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.totalElements()).isEqualTo(1);
            assertThat(resultPage.content())
                    .hasSize(1)
                    .first()
                    .satisfies(dto -> {
                        assertThat(dto.id()).isEqualTo(upcomingEvent.getId());
                        assertThat(dto.title()).isEqualTo(upcomingEvent.getTitle());
                        assertThat(dto.description()).isEqualTo(upcomingEvent.getDescription());
                        assertThat(dto.startDateTime()).isEqualTo(upcomingEvent.getStartDateTime());
                        assertThat(dto.endDateTime()).isEqualTo(upcomingEvent.getEndDateTime());
                        assertThat(dto.maxParticipants()).isEqualTo(upcomingEvent.getMaxParticipants());
                        assertThat(dto.registeredParticipants()).isEqualTo(upcomingEvent.getRegisteredParticipants());
                        assertThat(dto.imageUrl()).isEqualTo(upcomingEvent.getImageUrl());
                        assertThat(dto.eventUrl()).isEqualTo(upcomingEvent.getEventUrl());
                        assertThat(dto.location()).isEqualTo(upcomingEvent.getLocation());
                        assertThat(dto.is_remote()).isEqualTo(upcomingEvent.isRemote());
                        assertThat(dto.status()).isEqualTo(upcomingEvent.getStatus());
                    });
        }

        @Test
        @DisplayName("Should return an empty page when no upcoming events are found")
        void shouldReturnEmptyPage_whenNoUpcomingEventsFound() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Event> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(eventRepository.findUpcomingEvents(any(LocalDateTime.class), eq(pageable)))
                    .thenReturn(emptyPage);

            // Act
            PageResponseDTO<EventResponseDTO> resultPage = eventService.getUpcomingEvents(pageable);

            // Assert
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.totalElements()).isZero();
            assertThat(resultPage.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Tests for getEventDetails method")
    class GetEventDetailsTests {

        @Test
        @DisplayName("Should return event details for a valid ID")
        void shouldReturnEventDetails_whenValidId() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento Detalhado", LocalDateTime.now().plusDays(1), 100);

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(event));

            // Act
            EventResponseDTO resultDTO = eventService.getEventDetails(eventId);

            // Assert
            assertThat(resultDTO)
                    .isNotNull()
                    .satisfies(dto -> {
                        assertThat(dto.id()).isEqualTo(event.getId());
                        assertThat(dto.title()).isEqualTo(event.getTitle());
                        assertThat(dto.description()).isEqualTo(event.getDescription());
                        assertThat(dto.startDateTime()).isEqualTo(event.getStartDateTime());
                        assertThat(dto.endDateTime()).isEqualTo(event.getEndDateTime());
                        assertThat(dto.maxParticipants()).isEqualTo(event.getMaxParticipants());
                        assertThat(dto.registeredParticipants()).isEqualTo(event.getRegisteredParticipants());
                        assertThat(dto.imageUrl()).isEqualTo(event.getImageUrl());
                        assertThat(dto.eventUrl()).isEqualTo(event.getEventUrl());
                        assertThat(dto.location()).isEqualTo(event.getLocation());
                        assertThat(dto.is_remote()).isEqualTo(event.isRemote());
                        assertThat(dto.status()).isEqualTo(event.getStatus());
                    });
        }

        @Test
        @DisplayName("Should throw EventNotFoundException for an invalid ID")
        void shouldThrowEventNotFoundException_whenInvalidId() {
            // Arrange
            UUID invalidEventId = UUID.randomUUID();
            when(eventRepository.findById(invalidEventId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> eventService.getEventDetails(invalidEventId))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessage("Evento com ID " + invalidEventId + " não encontrado.");
        }
    }

    @Nested
    @DisplayName("Tests for cancelEvent method")
    class CancelEventTests {

        @Test
        @DisplayName("Should cancel an event and notify participants")
        void shouldCancelAndNotify_whenValidEvent() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento para Cancelar", LocalDateTime.now().plusDays(1), 10);
            assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(event));

            // Act
            eventService.cancelEvent(eventId);

            // Assert
            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
            verify(eventRepository, times(1)).save(event);
            verify(eventNotificationPort, times(1)).notifyParticipantsOfCancellation(event);
        }

        @Test
        @DisplayName("Should throw EventNotFoundException when cancelling a non-existent event")
        void shouldThrowEventNotFoundException_whenCancellingNonExistentEvent() {
            // Arrange
            UUID eventId = UUID.randomUUID();

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> eventService.cancelEvent(eventId))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessage("Evento com ID " + eventId + " não encontrado.");

            // Verifica que nenhuma ação de persistência ou notificação ocorreu
            verify(eventRepository, never()).save(any());
            verify(eventNotificationPort, never()).notifyParticipantsOfCancellation(any());
        }

        @Test
        @DisplayName("Should still cancel event even if notification fails")
        void shouldCancelEvent_whenNotificationFails() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento com Falha", LocalDateTime.now().plusDays(1), 10);
            assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(event));

            // Simula uma falha no serviço de notificação
            doThrow(new RuntimeException("Email service is down"))
                    .when(eventNotificationPort).notifyParticipantsOfCancellation(any(Event.class));

            // Act
            eventService.cancelEvent(eventId);

            // Assert
            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
            verify(eventRepository, times(1)).save(event);
            verify(eventNotificationPort, times(1)).notifyParticipantsOfCancellation(event);
        }

        @Test
        @DisplayName("Should propagate exception from entity when cancellation is invalid")
        void shouldPropagateException_whenEntityCancellationFails() {
            // Arrange
            UUID eventId = UUID.randomUUID();

            // Cria um evento que já está num estado que fará 'cancel()' falhar
            Event alreadyCancelledEvent = Event.builder()
                    .id(eventId)
                    .title("Evento Já Cancelado")
                    .status(EventStatus.CANCELLED)
                    .startDateTime(LocalDateTime.now().plusDays(1))
                    .endDateTime(LocalDateTime.now().plusDays(2))
                    .build();

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(alreadyCancelledEvent));

            // Act & Assert
            assertThatThrownBy(() -> eventService.cancelEvent(eventId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Este evento já está cancelado.");

            // Garante que nenhuma ação de persistência ou notificação ocorreu
            verify(eventRepository, never()).save(any(Event.class));
            verify(eventNotificationPort, never()).notifyParticipantsOfCancellation(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Tests for updateEvent method")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event successfully")
        void shouldUpdateEvent_whenValidData() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event existingEvent = createEventEntity("Old Title", LocalDateTime.now().plusDays(10), 100);
            Event existingEventSpy = spy(existingEvent); // Usa spy para verificar a chamada do método
            EventUpdateDTO updateDTO = new EventUpdateDTO("New Title", "New Description", null, null, 150, null, null, null, null);

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(existingEventSpy));
            when(eventRepository.save(any(Event.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(eventBusinessConfig.getMinDurationMinutes())
                    .thenReturn(15);

            // Act
            EventResponseDTO result = eventService.updateEvent(eventId, updateDTO);

            // Assert
            ArgumentCaptor<EventUpdateData> dataCaptor = ArgumentCaptor.forClass(EventUpdateData.class);
            verify(existingEventSpy).updateDetails(dataCaptor.capture(), eq(15));
            assertThat(dataCaptor.getValue().title()).isEqualTo(updateDTO.title());

            verify(eventRepository, times(1)).save(existingEventSpy);
            assertThat(result.title()).isEqualTo(updateDTO.title());
        }

        @Test
        @DisplayName("Should throw EventNotFoundException when event does not exist")
        void shouldThrowNotFoundException_whenEventDoesNotExist() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            EventUpdateDTO updateDTO = new EventUpdateDTO("New Title", null, null, null, null, null, null, null, null);

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> eventService.updateEvent(eventId, updateDTO))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessage("Evento com ID " + eventId + " não encontrado.");

            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("Should propagate exception from entity when update data is invalid")
        void shouldPropagateException_whenEntityUpdateFails() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event existingEvent = createEventEntity("Old Title", LocalDateTime.now().plusDays(10), 100);

            // Cria um "espião" para forçar um comportamento no método updateDetails
            Event existingEventSpy = spy(existingEvent);

            EventUpdateDTO invalidUpdateDTO = new EventUpdateDTO(null, null, null, null, 10, null, null, null, null); // Dados que causarão falha
            String errorMessage = "O número máximo de participantes não pode ser menor que o número de inscritos.";

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(existingEventSpy));
            when(eventBusinessConfig.getMinDurationMinutes())
                    .thenReturn(15);

            // Simula a falha na validação da entidade
            doThrow(new IllegalArgumentException(errorMessage))
                    .when(existingEventSpy).updateDetails(any(EventUpdateData.class), anyInt());

            // Act & Assert
            assertThatThrownBy(() -> eventService.updateEvent(eventId, invalidUpdateDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(errorMessage);

            // Garante que, como a operação falhou, nenhuma ação de persistência ocorreu
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Tests for registerParticipant method")
    class RegisterParticipantTests {

        @Test
        @DisplayName("Should register participant and send confirmation")
        void shouldSucceedAndNotify_whenValidData() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento para Registro", LocalDateTime.now().plusDays(1), 10);
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(event));
            when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com"))
                    .thenReturn(Optional.empty());

            // Act
            eventService.registerParticipant(eventId, subscriptionDTO);

            // Assert
            verify(subscriptionRepository, times(1)).save(any(Subscription.class));
            verify(eventRepository, times(1)).save(event);
            verify(eventNotificationPort, times(1)).sendRegistrationConfirmation(event, "test@example.com");
            assertThat(event.getRegisteredParticipants()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw SubscriptionAlreadyExistsException when participant is already registered")
        void shouldThrowSubscriptionAlreadyExistsException_whenAlreadyRegistered() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento com Duplicado", LocalDateTime.now().plusDays(1), 1);

            // Sanity check: Garante que o evento começa com 0 participantes
            assertThat(event.getRegisteredParticipants()).isEqualTo(0);

            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");
            Subscription existingSubscription = new Subscription(event, "test@example.com");

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(event));
            when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com"))
                    .thenReturn(Optional.of(existingSubscription));

            // Act & Assert
            assertThatThrownBy(() -> eventService.registerParticipant(eventId, subscriptionDTO))
                    .isInstanceOf(SubscriptionAlreadyExistsException.class)
                    .hasMessage("Este participante já está inscrito neste evento.");

            // Garante que nenhuma ação de persistência ou notificação ocorreu
            verify(subscriptionRepository, never()).save(any(Subscription.class));
            verify(eventRepository, never()).save(any(Event.class));
            verify(eventNotificationPort, never()).sendRegistrationConfirmation(any(Event.class), anyString());

            // Garante que o evento continua com 0 participantes
            assertThat(event.getRegisteredParticipants()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw Exception when entity throws it")
        void shouldPropagateException() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

            // Cria um evento que já está lotado usando o Builder.
            Event fullEvent = Event.builder()
                    .id(eventId)
                    .title("Full Event")
                    .startDateTime(LocalDateTime.now().plusDays(1))
                    .maxParticipants(50)
                    .registeredParticipants(50)
                    .status(EventStatus.ACTIVE)
                    .build();

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(fullEvent));
            when(subscriptionRepository.findByEventAndParticipantEmail(fullEvent, "test@example.com"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> eventService.registerParticipant(eventId, subscriptionDTO))
                    .isInstanceOf(EventFullException.class)
                    .hasMessage("O evento já está lotado.");

            // Garante que nenhuma ação de persistência ou notificação ocorreu
            verify(subscriptionRepository, never()).save(any(Subscription.class));
            verify(eventRepository, never()).save(any(Event.class));
            verify(eventNotificationPort, never()).sendRegistrationConfirmation(any(Event.class), anyString());

            assertThat(fullEvent.getRegisteredParticipants()).isEqualTo(50);
        }

        @Test
        @DisplayName("Should throw EventNotFoundException when event does not exist")
        void shouldThrowEventNotFoundException_whenEventIsNotFound() {
            // Arrange
            UUID nonExistentEventId = UUID.randomUUID();
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

            // Simula o repositório não encontrando o evento
            when(eventRepository.findById(nonExistentEventId))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> eventService.registerParticipant(nonExistentEventId, subscriptionDTO))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessage("Evento com ID " + nonExistentEventId + " não encontrado.");

            // Garante que nenhuma outra interação com o banco de dados ou notificação ocorreu
            verify(subscriptionRepository, never()).findByEventAndParticipantEmail(any(), any());
            verify(subscriptionRepository, never()).save(any());
            verify(eventRepository, never()).save(any());
            verify(eventNotificationPort, never()).sendRegistrationConfirmation(any(), any());
        }

        @Test
        @DisplayName("Should register participant successfully even if notification fails")
        void shouldRegister_whenNotificationFails() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento com Falha de Email", LocalDateTime.now().plusDays(1), 10);
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

            when(eventRepository.findById(eventId))
                    .thenReturn(Optional.of(event));
            when(subscriptionRepository.findByEventAndParticipantEmail(event, "test@example.com"))
                    .thenReturn(Optional.empty());

            // Simula uma falha no serviço de notificação
            doThrow(new RuntimeException("Email service is down"))
                    .when(eventNotificationPort).sendRegistrationConfirmation(any(Event.class), anyString());

            // Act
            // O método deve executar sem lançar uma exceção
            eventService.registerParticipant(eventId, subscriptionDTO);

            // Assert
            // Verifica se as operações de salvamento ainda ocorreram
            verify(subscriptionRepository, times(1)).save(any(Subscription.class));
            verify(eventRepository, times(1)).save(event);

            // Verifica que a tentativa de notificação foi feita (mesmo que tenha falhado)
            verify(eventNotificationPort, times(1)).sendRegistrationConfirmation(event, "test@example.com");
        }
    }

    @Nested
    @DisplayName("Tests for getRegisteredParticipants method")
    class GetRegisteredParticipantsTests {

        @Test
        @DisplayName("Should return a page of participants for a valid event")
        void shouldReturnParticipantsPage_whenValidEventId() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento com Participantes", LocalDateTime.now().plusDays(1), 100);
            Subscription subscription = new Subscription(event, "test@example.com");
            Pageable pageable = PageRequest.of(0, 10);
            Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription), pageable, 1);

            when(eventRepository.existsById(eventId))
                    .thenReturn(true);
            when(eventRepository.getReferenceById(eventId))
                    .thenReturn(event);
            when(subscriptionRepository.findByEvent(event, pageable))
                    .thenReturn(subscriptionPage);

            // Act
            PageResponseDTO<RegisteredParticipantDTO> resultPage = eventService.getRegisteredParticipants(eventId, pageable);

            // Assert
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.totalElements()).isEqualTo(1);
            assertThat(resultPage.content())
                    .hasSize(1)
                    .first()
                    .extracting(RegisteredParticipantDTO::participantEmail)
                    .isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw EventNotFoundException for an invalid event ID")
        void shouldThrowEventNotFoundException_whenInvalidEventId() {
            // Arrange
            UUID invalidEventId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            when(eventRepository.existsById(invalidEventId))
                    .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> eventService.getRegisteredParticipants(invalidEventId, pageable))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessage("Evento com ID " + invalidEventId + " não encontrado.");

            verify(eventRepository, never()).getReferenceById(invalidEventId);
            verify(subscriptionRepository, never()).findByEvent(any(Event.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return an empty page when event has no participants")
        void shouldReturnEmptyPage_whenEventHasNoParticipants() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento Vazio", LocalDateTime.now().plusDays(1), 100);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Subscription> emptySubscriptionPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(eventRepository.existsById(eventId))
                    .thenReturn(true);
            when(eventRepository.getReferenceById(eventId))
                    .thenReturn(event);
            when(subscriptionRepository.findByEvent(event, pageable))
                    .thenReturn(emptySubscriptionPage);

            // Act
            PageResponseDTO<RegisteredParticipantDTO> resultPage = eventService.getRegisteredParticipants(eventId, pageable);

            // Assert
            assertThat(resultPage).isNotNull();
            assertThat(resultPage.totalElements()).isZero();
            assertThat(resultPage.content()).isEmpty();
        }

        @Test
        @DisplayName("Should propagate exception when fetching participants fails")
        void shouldPropagateException_whenFetchingParticipantsFails() {
            // Arrange
            UUID eventId = UUID.randomUUID();
            Event event = createEventEntity("Evento com Erro", LocalDateTime.now().plusDays(1), 100);
            Pageable pageable = PageRequest.of(0, 10);

            when(eventRepository.existsById(eventId))
                    .thenReturn(true); // Evento existe
            when(eventRepository.getReferenceById(eventId))
                    .thenReturn(event); // Retorna o proxy do evento

            // Simula uma falha ao buscar as inscrições
            doThrow(new RuntimeException("Database connection lost"))
                    .when(subscriptionRepository).findByEvent(any(Event.class), any(Pageable.class));

            // Act & Assert
            assertThatThrownBy(() -> eventService.getRegisteredParticipants(eventId, pageable))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection lost");

            verify(eventRepository, times(1)).existsById(eventId);
            verify(eventRepository, times(1)).getReferenceById(eventId);
            verify(subscriptionRepository, times(1)).findByEvent(event, pageable);
        }
    }
}
