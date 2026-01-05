package com.Samuel.event_microservice.infrastructure.application;

import com.Samuel.event_microservice.core.data.EventUpdateData;
import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.core.ports.EventNotificationPort;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import com.Samuel.event_microservice.core.usecases.EventUseCase;
import com.Samuel.event_microservice.infrastructure.config.EventBusinessConfig;
import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventUpdateDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementação principal da interface {@link EventUseCase}.
 * <p>
 * Esta classe orquestra as interações com as portas de saída (repositórios, etc.)
 * para executar as regras de negócio da aplicação, sem conhecer os detalhes da infraestrutura.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService implements EventUseCase {

    private final EventRepositoryPort eventRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final EventNotificationPort eventNotificationPort;
    private final EventBusinessConfig eventConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO eventRequest) {
        log.info("Creating a new event with title: {}", eventRequest.title());
        Event newEvent = new Event(
                eventRequest.title(),
                eventRequest.description(),
                eventRequest.startDateTime(),
                eventRequest.endDateTime(),
                eventRequest.maxParticipants(),
                eventRequest.imageUrl(),
                eventRequest.eventUrl(),
                eventRequest.location(),
                eventRequest.is_remote(),
                eventConfig.getMinDurationMinutes()
        );
        eventRepository.save(newEvent);
        log.info("Event created successfully with ID: {}", newEvent.getId());
        return new EventResponseDTO(newEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventResponseDTO> getAllEvents(Pageable pageable) {
        log.info("Fetching all events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Event> eventPage = eventRepository.findAll(pageable);
        log.info("Found {} total events.", eventPage.getTotalElements());
        Page<EventResponseDTO> eventResponseDTOPage = eventPage.map(EventResponseDTO::new);
        return new PageResponseDTO<>(eventResponseDTOPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventResponseDTO> getUpcomingEvents(Pageable pageable) {
        log.info("Fetching upcoming events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Event> eventPage = eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
        log.info("Found {} upcoming events.", eventPage.getTotalElements());
        Page<EventResponseDTO> eventResponseDTOPage = eventPage.map(EventResponseDTO::new);
        return new PageResponseDTO<>(eventResponseDTOPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventDetails(UUID id) {
        log.info("Fetching details for event with ID: {}", id);
        return eventRepository.findById(id)
                .map(EventResponseDTO::new)
                .orElseThrow(() -> {
                    log.warn("Event with ID {} not found.", id);
                    return new EventNotFoundException("Evento com ID " + id + " não encontrado.");
                });
    }

    /**
     * {@inheritDoc}
     * <p>
     * Este método implementa a lógica de cancelamento (Soft Delete).
     * Ele verifica se o evento existe, se já ocorreu ou se já está cancelado antes de alterar o status.
     */
    @Override
    @Transactional
    public void cancelEvent(UUID eventId) {
        log.info("Attempting to cancel event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Cancellation failed: Event with ID {} not found.", eventId);
                    return new EventNotFoundException("Evento com ID " + eventId + " não encontrado.");
                });

        event.cancel();
        
        eventRepository.save(event);
        log.info("Event with ID {} cancelled successfully.", eventId);

        try {
            eventNotificationPort.notifyParticipantsOfCancellation(event);
        } catch (Exception e) {
            log.error("Failed to send cancellation notifications for event {}: {}", eventId, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EventResponseDTO updateEvent(UUID eventId, EventUpdateDTO eventUpdateDTO) {
        log.info("Attempting to update event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Update failed: Event with ID {} not found.", eventId);
                    return new EventNotFoundException("Evento com ID " + eventId + " não encontrado.");
                });

        EventUpdateData updateData = new EventUpdateData(
                eventUpdateDTO.title(),
                eventUpdateDTO.description(),
                eventUpdateDTO.startDateTime(),
                eventUpdateDTO.endDateTime(),
                eventUpdateDTO.maxParticipants(),
                eventUpdateDTO.imageUrl(),
                eventUpdateDTO.eventUrl(),
                eventUpdateDTO.location(),
                eventUpdateDTO.is_remote()
        );

        event.updateDetails(updateData, eventConfig.getMinDurationMinutes());

        Event updatedEvent = eventRepository.save(event);
        log.info("Event with ID {} updated successfully.", eventId);
        return new EventResponseDTO(updatedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementação é transacional. Ela verifica a existência do evento, se ele está ativo,
     * se a inscrição já existe e se há vagas disponíveis.
     * Após o sucesso da transação, tenta enviar um e-mail de confirmação.
     */
    @Override
    @Transactional
    public void registerParticipant(UUID eventId, SubscriptionRequestDTO subscriptionRequest) {
        String participantEmail = subscriptionRequest.participantEmail();
        log.info("Attempting to register participant {} for event {}", participantEmail, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Registration failed: Event with ID {} not found.", eventId);
                    return new EventNotFoundException("Evento com ID " + eventId + " não encontrado.");
                });
        
        subscriptionRepository.findByEventAndParticipantEmail(event, participantEmail)
                .ifPresent(subscription -> {
                    log.warn("Participant {} is already subscribed to event {}.", participantEmail, eventId);
                    throw new SubscriptionAlreadyExistsException("Este participante já está inscrito neste evento.");
                });

        event.registerParticipant();

        Subscription newSubscription = new Subscription(event, participantEmail);
        subscriptionRepository.save(newSubscription);
        eventRepository.save(event);
        log.info("Participant {} registered successfully for event {}.", participantEmail, eventId);

        try {
            eventNotificationPort.sendRegistrationConfirmation(event, participantEmail);
        } catch (Exception e) {
            log.error("Failed to send registration confirmation email to {} for event {}: {}", participantEmail, eventId, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementação otimiza a busca verificando primeiro a existência do evento
     * antes de buscar a página de inscrições.
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<RegisteredParticipantDTO> getRegisteredParticipants(UUID eventId, Pageable pageable) {
        log.info("Fetching participants for event with ID: {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            log.warn("Event with ID {} not found when fetching participants.", eventId);
            throw new EventNotFoundException("Evento com ID " + eventId + " não encontrado.");
        }

        Event event = eventRepository.getReferenceById(eventId);
        Page<Subscription> subscriptions = subscriptionRepository.findByEvent(event, pageable);
        log.info("Found {} participants for event {}.", subscriptions.getTotalElements(), eventId);
        Page<RegisteredParticipantDTO> registeredParticipantDTOPage = subscriptions.map(subscription -> new RegisteredParticipantDTO(subscription.getParticipantEmail()));
        return new PageResponseDTO<>(registeredParticipantDTOPage);
    }
}
