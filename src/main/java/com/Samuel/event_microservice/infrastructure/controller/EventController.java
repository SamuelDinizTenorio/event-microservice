package com.Samuel.event_microservice.infrastructure.controller;

import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.SuccessResponseDTO;
import com.Samuel.event_microservice.core.usecases.EventUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final EventUseCase eventUseCase;

    /**
     * Retorna uma página de todos os eventos (futuros e passados), ordenados por data.
     *
     * @param pageable Objeto injetado pelo Spring para controle de paginação e ordenação.
     * @return Um {@link ResponseEntity} com status 200 OK e um DTO de resposta paginada.
     */
    @GetMapping
    public ResponseEntity<PageResponseDTO<EventResponseDTO>> getAllEvents(
            @PageableDefault(sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
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
            @PageableDefault(sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponseDTO<EventResponseDTO> events = eventUseCase.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retorna os detalhes de um evento específico pelo seu ID.
     *
     * @param id O UUID do evento, fornecido como uma variável de caminho.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link EventResponseDTO} do evento.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventDetails(@PathVariable UUID id) {
        EventResponseDTO event = eventUseCase.getEventDetails(id);
        return ResponseEntity.ok(event);
    }

    /**
     * Cria um novo evento com base nos dados fornecidos no corpo da requisição.
     *
     * @param eventRequest O DTO com os dados para a criação do evento, validado pelo framework.
     * @return Um {@link ResponseEntity} com status 201 Created, o header 'Location' para o novo recurso,
     *         e o {@link EventResponseDTO} do evento criado no corpo.
     */
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody @Valid EventRequestDTO eventRequest) {
        EventResponseDTO createdEvent = eventUseCase.createEvent(eventRequest);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdEvent.id())
                .toUri();
        
        return ResponseEntity.created(uri).body(createdEvent);
    }

    /**
     * Registra um participante em um evento específico.
     *
     * @param eventId O UUID do evento, fornecido como uma variável de caminho.
     * @param subscriptionRequest O DTO com o e-mail do participante a ser inscrito.
     * @return Um {@link ResponseEntity} com status 200 OK e uma mensagem de sucesso.
     */
    @PostMapping("/{eventId}/register")
    public ResponseEntity<SuccessResponseDTO> registerParticipant(@PathVariable UUID eventId,
                                                                  @RequestBody @Valid SubscriptionRequestDTO subscriptionRequest) {
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
        PageResponseDTO<RegisteredParticipantDTO> participants = eventUseCase.getRegisteredParticipants(eventId, pageable);
        return ResponseEntity.ok(participants);
    }
}
