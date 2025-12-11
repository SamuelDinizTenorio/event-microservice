package com.Samuel.event_microservice.controller;

import com.Samuel.event_microservice.domain.Event;
import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.dto.EventResponseDTO;
import com.Samuel.event_microservice.dto.RegisteredParticipantDTO;
import com.Samuel.event_microservice.dto.SubscriptionRequestDTO;
import com.Samuel.event_microservice.services.EventService;
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

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EventResponseDTO> events = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable UUID id) {
        EventResponseDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventResponseDTO>> getUpcomingEvents(
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EventResponseDTO> events = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody @Valid EventRequestDTO eventRequest) {
        Event newEvent = eventService.createEvent(eventRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEvent.getId())
                .toUri();
        return ResponseEntity.created(uri).body(newEvent);
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<RegisteredParticipantDTO> registerParticipant(@PathVariable UUID eventId,
                                                                        @RequestBody @Valid SubscriptionRequestDTO subscriptionRequest) {
        eventService.registerParticipant(eventId, subscriptionRequest.participantEmail());
        RegisteredParticipantDTO response = new RegisteredParticipantDTO("Participante registrado com sucesso.");
        return ResponseEntity.ok(response);
    }
}
