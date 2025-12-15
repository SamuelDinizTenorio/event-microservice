package com.Samuel.event_microservice.services;

import com.Samuel.event_microservice.domain.Event;
import com.Samuel.event_microservice.domain.Subscription;
import com.Samuel.event_microservice.dto.*;
import com.Samuel.event_microservice.exceptions.EventFullException;
import com.Samuel.event_microservice.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.repositories.EventRepository;
import com.Samuel.event_microservice.repositories.SubscriptionRepository;
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
 * Serviço que encapsula a lógica de negócio para gerenciamento de eventos.
 * <p>
 * Esta classe é responsável por criar, buscar e gerenciar inscrições em eventos,
 * além de se comunicar com outros serviços, como o de e-mail.
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailServiceClient emailServiceClient;

    /**
     * Cria um novo evento com base nos dados fornecidos.
     * A operação é transacional.
     *
     * @param eventRequest O DTO contendo os detalhes do evento a ser criado.
     * @return O evento que foi criado e salvo no banco de dados.
     */
    @Transactional
    public Event createEvent(EventRequestDTO eventRequest) {
        logger.info("Creating a new event with title: {}", eventRequest.title());
        Event newEvent = new Event(eventRequest);
        eventRepository.save(newEvent);
        logger.info("Event created successfully with ID: {}", newEvent.getId());
        return newEvent;
    }

    /**
     * Busca uma página de todos os eventos (futuros e passados).
     * A transação é somente leitura para otimização.
     *
     * @param pageable O objeto de paginação.
     * @return Uma {@link Page} de {@link EventResponseDTO}.
     */
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getAllEvents(Pageable pageable) {
        logger.info("Fetching all events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Event> eventPage = eventRepository.findAll(pageable);
        logger.info("Found {} total events.", eventPage.getTotalElements());
        return eventPage.map(EventResponseDTO::new);
    }

    /**
     * Busca uma página de eventos futuros.
     * A transação é somente leitura para otimização.
     *
     * @param pageable O objeto de paginação.
     * @return Uma {@link Page} de {@link EventResponseDTO} representando os eventos futuros.
     */
    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getUpcomingEvents(Pageable pageable) {
        logger.info("Fetching upcoming events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Event> eventPage = eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
        logger.info("Found {} upcoming events.", eventPage.getTotalElements());
        return eventPage.map(EventResponseDTO::new);
    }

    /**
     * Busca os detalhes de um evento específico pelo seu ID.
     * A transação é somente leitura para otimização.
     *
     * @param id O UUID do evento a ser buscado.
     * @return Um {@link EventResponseDTO} com os detalhes do evento.
     * @throws EventNotFoundException se o evento com o ID fornecido não for encontrado.
     */
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
     * Registra um participante em um evento específico.
     * Este método é transacional para garantir a atomicidade da operação de inscrição.
     *
     * @param eventId O UUID do evento no qual o participante será registrado.
     * @param subscriptionRequest O DTO contendo o e-mail do participante.
     * @throws EventNotFoundException se o evento com o ID fornecido não for encontrado.
     * @throws SubscriptionAlreadyExistsException se o participante já estiver inscrito no evento.
     * @throws EventFullException se o evento já estiver lotado.
     */
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
            emailServiceClient.sendEmail(emailRequest);
            logger.info("Confirmation email sent successfully to {}.", participantEmail);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {} for event {}: {}", participantEmail, eventId, e.getMessage());
            // Nota: A falha no envio de e-mail não reverte a transação da inscrição.
            // Isso pode ser uma decisão de negócio (a inscrição é mais importante)
            // ou pode exigir uma lógica de compensação/re-tentativa mais complexa.
        }
    }

    /**
     * Busca uma página de participantes registrados em um evento específico.
     * A transação é somente leitura para otimização.
     *
     * @param eventId O UUID do evento.
     * @param pageable O objeto de paginação.
     * @return Uma {@link Page} de {@link RegisteredParticipantDTO} com os e-mails dos participantes.
     * @throws EventNotFoundException se o evento com o ID fornecido não for encontrado.
     */
    @Transactional(readOnly = true)
    public Page<RegisteredParticipantDTO> getRegisteredParticipants(UUID eventId, Pageable pageable) {
        logger.info("Fetching participants for event with ID: {}", eventId);
        if (!eventRepository.existsById(eventId)) {
            logger.warn("Event with ID {} not found when fetching participants.", eventId);
            throw new EventNotFoundException("Evento com ID " + eventId + " não encontrado.");
        }

        Page<Subscription> subscriptions = subscriptionRepository.findByEvent(eventRepository.getReferenceById(eventId), pageable);
        logger.info("Found {} participants for event {}.", subscriptions.getTotalElements(), eventId);
        return subscriptions.map(subscription -> new RegisteredParticipantDTO(subscription.getParticipantEmail()));
    }
}
