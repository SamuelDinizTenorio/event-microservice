package com.Samuel.event_microservice.controller;

import com.Samuel.event_microservice.domain.Event;
import com.Samuel.event_microservice.dto.ErrorResponseDTO;
import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.dto.EventResponseDTO;
import com.Samuel.event_microservice.dto.RegisteredParticipantDTO;
import com.Samuel.event_microservice.dto.SubscriptionRequestDTO;
import com.Samuel.event_microservice.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Controlador REST para gerenciar operações relacionadas a eventos.
 * Fornece endpoints para criar, buscar e registrar participantes em eventos.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "API para gerenciamento de eventos e inscrições")
public class EventController {

    private final EventService eventService;

    /**
     * Retorna uma página de todos os eventos (futuros e passados).
     * Os eventos são ordenados pela data em ordem ascendente por padrão.
     *
     * @param pageable Objeto Pageable para paginação e ordenação.
     * @return ResponseEntity contendo uma página de EventResponseDTO.
     */
    @Operation(summary = "Lista todos os eventos", description = "Retorna uma página de todos os eventos, incluindo futuros e passados.")
    @ApiResponse(responseCode = "200", description = "Página de eventos retornada com sucesso.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)))
    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @PageableDefault(sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<EventResponseDTO> events = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retorna uma página de eventos futuros.
     * Os eventos são ordenados pela data em ordem ascendente por padrão.
     *
     * @param pageable Objeto Pageable para paginação e ordenação.
     * @return ResponseEntity contendo uma página de EventResponseDTO.
     */
    @Operation(summary = "Lista eventos futuros", description = "Retorna uma página de eventos que ainda não ocorreram.")
    @ApiResponse(responseCode = "200", description = "Página de eventos futuros retornada com sucesso.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)))
    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventResponseDTO>> getUpcomingEvents(
            @PageableDefault(sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<EventResponseDTO> events = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * Retorna os detalhes de um evento específico pelo seu ID.
     *
     * @param id O UUID do evento.
     * @return ResponseEntity contendo o EventResponseDTO do evento.
     */
    @Operation(summary = "Obtém detalhes de um evento", description = "Retorna os detalhes completos de um evento específico pelo seu ID.")
    @ApiResponse(responseCode = "200", description = "Detalhes do evento retornados com sucesso.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Evento não encontrado.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventDetails(@PathVariable UUID id) {
        EventResponseDTO event = eventService.getEventDetails(id);
        return ResponseEntity.ok(event);
    }

    /**
     * Cria um novo evento.
     *
     * @param eventRequest O DTO com os dados para criação do evento.
     * @return ResponseEntity com o evento criado e o status 201 Created.
     */
    @Operation(summary = "Cria um novo evento", description = "Cria um novo evento com os dados fornecidos e retorna o evento criado.")
    @ApiResponse(responseCode = "201", description = "Evento criado com sucesso.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Event.class)))
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody @Valid EventRequestDTO eventRequest) {
        Event newEvent = eventService.createEvent(eventRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEvent.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newEvent);
    }

    /**
     * Registra um participante em um evento específico.
     *
     * @param eventId O UUID do evento.
     * @param subscriptionRequest O DTO com o e-mail do participante.
     * @return ResponseEntity com status 204 No Content.
     */
    @Operation(summary = "Registra um participante em um evento", description = "Registra um participante no evento especificado pelo ID.")
    @ApiResponse(responseCode = "204", description = "Participante registrado com sucesso.")
    @ApiResponse(responseCode = "400", description = "Requisição inválida (ex: evento lotado, e-mail inválido).",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Evento não encontrado.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    @PostMapping("/{eventId}/register")
    public ResponseEntity<Void> registerParticipant(@PathVariable UUID eventId,
                                                    @RequestBody @Valid SubscriptionRequestDTO subscriptionRequest) {
        eventService.registerParticipant(eventId, subscriptionRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna a lista paginada de participantes registrados para um evento específico.
     * A ordenação pode ser feita pelos campos 'createdAt' e 'participantEmail'.
     *
     * @param eventId O UUID do evento.
     * @param pageable Objeto Pageable para paginação e ordenação.
     * @return ResponseEntity contendo uma página de RegisteredParticipantDTO.
     */
    @Operation(summary = "Lista participantes de um evento", description = "Retorna uma página com todos os participantes registrados para um evento específico.")
    @ApiResponse(responseCode = "200", description = "Página de participantes retornada com sucesso.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "404", description = "Evento não encontrado.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    @GetMapping("/{eventId}/participants")
    public ResponseEntity<Page<RegisteredParticipantDTO>> getRegisteredParticipants(
            @PathVariable UUID eventId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<RegisteredParticipantDTO> participants = eventService.getRegisteredParticipants(eventId, pageable);
        return ResponseEntity.ok(participants);
    }
}
