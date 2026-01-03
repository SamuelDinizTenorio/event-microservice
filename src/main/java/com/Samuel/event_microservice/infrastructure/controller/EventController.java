package com.Samuel.event_microservice.infrastructure.controller;

import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventUpdateDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.SuccessResponseDTO;
import com.Samuel.event_microservice.core.usecases.EventUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Controlador REST que expõe os endpoints para gerenciamento de eventos.
 * <p>
 * Esta classe atua como a camada de entrada da aplicação para requisições HTTP,
 * delegando a execução da lógica de negócio para a camada de casos de uso ({@link EventUseCase}).
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventUseCase eventUseCase;

    /**
     * Retorna uma página de todos os eventos (futuros e passados), ordenados por data.
     *
     * @param pageable Objeto injetado pelo Spring para controle de paginação e ordenação.
     * @return Um {@link ResponseEntity} com status 200 OK e um DTO de resposta paginada.
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getAllEvents(
            @PageableDefault(sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("Received request to get all events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        PageResponseDTO<EventResponseDTO> events = eventUseCase.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retorna uma página de eventos futuros, ordenados por data.
     *
     * @param pageable Objeto injetado pelo Spring para controle de paginação e ordenação.
     * @return Um {@link ResponseEntity} com status 200 OK e um DTO de resposta paginada.
     */
    @GetMapping("/upcoming")
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getUpcomingEvents(
            @PageableDefault(sort = "startDateTime", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("Received request to get upcoming events. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        PageResponseDTO<EventResponseDTO> events = eventUseCase.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retorna os detalhes de um evento específico pelo seu ID.
     *
     * @param eventId O UUID do evento, fornecido como uma variável de caminho.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link EventResponseDTO} do evento.
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEventDetails(@PathVariable UUID eventId) {
        logger.info("Received request to get details for event with ID: {}", eventId);
        EventResponseDTO event = eventUseCase.getEventDetails(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * Cria um evento com base nos dados fornecidos no corpo da requisição.
     *
     * @param eventRequest O DTO com os dados para a criação do evento, validado pelo framework.
     * @return Um {@link ResponseEntity} com status 201 Created, o header 'Location' para o novo recurso,
     *         e o {@link EventResponseDTO} do evento criado no corpo.
     */
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody @Valid EventRequestDTO eventRequest) {
        logger.info("Received request to create a new event with title: {}", eventRequest.title());
        EventResponseDTO createdEvent = eventUseCase.createEvent(eventRequest);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{eventId}")
                .buildAndExpand(createdEvent.id())
                .toUri();
        
        return ResponseEntity.created(uri).body(createdEvent);
    }

    /**
     * Cancela um evento, marcando o seu status como CANCELLED.
     *
     * @param eventId O UUID do evento a ser cancelado.
     * @return Um {@link ResponseEntity} com status 200 OK e uma mensagem de sucesso.
     */
    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<SuccessResponseDTO> cancelEvent(@PathVariable UUID eventId) {
        logger.info("Received request to cancel event with ID: {}", eventId);
        eventUseCase.cancelEvent(eventId);
        SuccessResponseDTO response = new SuccessResponseDTO("Evento cancelado com sucesso!");
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza parcialmente um evento existente com base nos dados fornecidos.
     * Apenas os campos não nulos do DTO serão considerados para atualização.
     *
     * @param eventId O UUID do evento a ser atualizado.
     * @param eventUpdateDTO O DTO com os campos a serem atualizados.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link EventResponseDTO} do evento atualizado.
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventUpdateDTO eventUpdateDTO) {
        logger.info("Received request to update event with ID: {}", eventId);
        EventResponseDTO updatedEvent = eventUseCase.updateEvent(eventId, eventUpdateDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Registra um participante em um evento específico.
     *
     * @param eventId O UUID do evento, fornecido como uma variável de caminho.
     * @param subscriptionRequest O DTO com o e-mail do participante a ser inscrito.
     * @return Um {@link ResponseEntity} com status 200 OK e uma mensagem de sucesso.
     */
    @PostMapping("/{eventId}/register")
    public ResponseEntity<SuccessResponseDTO> registerParticipant(
            @PathVariable UUID eventId,
            @RequestBody @Valid SubscriptionRequestDTO subscriptionRequest) {
        logger.info("Received request to register participant {} for event {}", subscriptionRequest.participantEmail(), eventId);
        eventUseCase.registerParticipant(eventId, subscriptionRequest);
        SuccessResponseDTO response = new SuccessResponseDTO("Inscrição realizada com sucesso!");
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna a lista paginada de participantes registrados para um evento específico.
     *
     * @param eventId O UUID do evento, fornecido como uma variável de caminho.
     * @param pageable Objeto injetado pelo Spring para controle de paginação e ordenação.
     * @return Um {@link ResponseEntity} com status 200 OK e um DTO de resposta paginada.
     */
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<PageResponseDTO<RegisteredParticipantDTO>> getRegisteredParticipants(
            @PathVariable UUID eventId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("Received request to get participants for event {}. Page: {}, Size: {}", eventId, pageable.getPageNumber(), pageable.getPageSize());
        PageResponseDTO<RegisteredParticipantDTO> participants = eventUseCase.getRegisteredParticipants(eventId, pageable);
        return ResponseEntity.ok(participants);
    }
}
