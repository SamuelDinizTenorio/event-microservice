package com.Samuel.event_microservice.infrastructure.application;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.core.exceptions.EventFullException;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.core.ports.EmailSender;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import com.Samuel.event_microservice.core.usecases.EventUseCase;
import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class EventService implements EventUseCase {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepositoryPort eventRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final EmailSender emailSender;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO eventRequest) {
        logger.info("Creating a new event with title: {}", eventRequest.title());
        Event newEvent = new Event(
                eventRequest.title(),
                eventRequest.description(),
                eventRequest.date(),
                eventRequest.maxParticipants(),
                eventRequest.imageUrl(),
                eventRequest.eventUrl(),
                eventRequest.location(),
                eventRequest.isRemote()
        );
        eventRepository.save(newEvent);
        logger.info("Event created successfully with ID: {}", newEvent.getId());
        return new EventResponseDTO(newEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventResponseDTO> getAllEvents(Pageable pageable) {
        logger.info("Fetching all events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Event> eventPage = eventRepository.findAll(pageable);
        logger.info("Found {} total events.", eventPage.getTotalElements());
        Page<EventResponseDTO> eventResponseDTOPage = eventPage.map(EventResponseDTO::new);
        return new PageResponseDTO<>(eventResponseDTOPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<EventResponseDTO> getUpcomingEvents(Pageable pageable) {
        logger.info("Fetching upcoming events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Event> eventPage = eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
        logger.info("Found {} upcoming events.", eventPage.getTotalElements());
        Page<EventResponseDTO> eventResponseDTOPage = eventPage.map(EventResponseDTO::new);
        return new PageResponseDTO<>(eventResponseDTOPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventDetails(UUID id) {
        logger.info("Fetching details for event with ID: {}", id);
        return eventRepository.findById(id)
                .map(EventResponseDTO::new)
                .orElseThrow(() -> {
                    logger.warn("Event with ID {} not found.", id);
                    return new EventNotFoundException("Evento com ID " + id + " não encontrado.");
                });
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementação é transacional. Ela verifica a existência do evento e da inscrição,
     * atualiza o contador de participantes e salva a nova inscrição. Após o sucesso da
     * transação, tenta enviar um e-mail de confirmação. Uma falha no envio do e-mail
     * não reverte a inscrição.
     */
    @Override
    @Transactional
    public void registerParticipant(UUID eventId, SubscriptionRequestDTO subscriptionRequest) {
        String participantEmail = subscriptionRequest.participantEmail();
        logger.info("Attempting to register participant {} for event {}", participantEmail, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    logger.warn("Registration failed: Event with ID {} not found.", eventId);
                    return new EventNotFoundException("Evento com ID " + eventId + " não encontrado.");
                });

        subscriptionRepository.findByEventAndParticipantEmail(event, participantEmail)
                .ifPresent(subscription -> {
                    logger.warn("Participant {} is already subscribed to event {}.", participantEmail, eventId);
                    throw new SubscriptionAlreadyExistsException("Este participante já está inscrito neste evento.");
                });

        try {
            event.registerParticipant();
        } catch (EventFullException e) {
            logger.warn("Registration failed: Event {} is full.", eventId);
            throw e;
        }

        Subscription newSubscription = new Subscription(event, participantEmail);
        subscriptionRepository.save(newSubscription);
        eventRepository.save(event);
        logger.info("Participant {} registered successfully for event {}.", participantEmail, eventId);

        try {
            logger.info("Sending confirmation email to {}.", participantEmail);
            EmailRequestDTO emailRequest = new EmailRequestDTO(
                    participantEmail,
                    "Inscrição Confirmada: " + event.getTitle(),
                    "Sua inscrição no evento '" + event.getTitle() + "' foi confirmada com sucesso!"
            );
            emailSender.sendEmail(emailRequest);
            logger.info("Confirmation email sent successfully to {}.", participantEmail);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {} for event {}: {}", participantEmail, eventId, e.getMessage());
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
        logger.info("Fetching participants for event with ID: {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            logger.warn("Event with ID {} not found when fetching participants.", eventId);
            throw new EventNotFoundException("Evento com ID " + eventId + " não encontrado.");
        }

        Page<Subscription> subscriptions = subscriptionRepository.findByEvent(eventRepository.getReferenceById(eventId), pageable);
        logger.info("Found {} participants for event {}.", subscriptions.getTotalElements(), eventId);
        Page<RegisteredParticipantDTO> registeredParticipantDTOPage = subscriptions.map(subscription -> new RegisteredParticipantDTO(subscription.getParticipantEmail()));
        return new PageResponseDTO<>(registeredParticipantDTOPage);
    }
}
