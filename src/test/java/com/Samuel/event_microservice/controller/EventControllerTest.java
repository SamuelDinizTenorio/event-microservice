package com.Samuel.event_microservice.controller;

import com.Samuel.event_microservice.domain.Event;
import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.dto.EventResponseDTO;
import com.Samuel.event_microservice.dto.RegisteredParticipantDTO;
import com.Samuel.event_microservice.dto.SubscriptionRequestDTO;
import com.Samuel.event_microservice.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @Test
    @DisplayName("Should return status 200 and a page of all events when calling GET /events")
    void getAllEvents_shouldReturnPageOfEvents() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        EventResponseDTO eventDTO = new EventResponseDTO(eventId, "Evento de Teste", "Descrição", LocalDateTime.now(), 100, 10, "http://image.url", "http://event.url", null, true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<EventResponseDTO> eventPage = new PageImpl<>(List.of(eventDTO), pageable, 1);

        when(eventService.getAllEvents(any(Pageable.class))).thenReturn(eventPage);

        // Act & Assert
        mockMvc.perform(get("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Evento de Teste"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return status 200 and a page of upcoming events when calling GET /events/upcoming")
    void getUpcomingEvents_shouldReturnPageOfUpcomingEvents() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        EventResponseDTO upcomingEventDTO = new EventResponseDTO(eventId, "Evento Futuro", "Descrição", LocalDateTime.now().plusDays(5), 50, 5, "http://image.url", "http://event.url", null, true);
        Pageable pageable = PageRequest.of(0, 5);
        Page<EventResponseDTO> eventPage = new PageImpl<>(List.of(upcomingEventDTO), pageable, 1);

        when(eventService.getUpcomingEvents(any(Pageable.class))).thenReturn(eventPage);

        // Act & Assert
        mockMvc.perform(get("/events/upcoming")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Evento Futuro"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return status 200 and event details when calling GET /events/{id} with a valid ID")
    void getEventDetails_withValidId_shouldReturnEventDetails() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        EventResponseDTO eventDTO = new EventResponseDTO(eventId, "Evento Detalhado", "Descrição", LocalDateTime.now(), 100, 25, "http://image.url", null, "Local", false);

        when(eventService.getEventDetails(eventId)).thenReturn(eventDTO);

        // Act & Assert
        mockMvc.perform(get("/events/{id}", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Evento Detalhado"))
                .andExpect(jsonPath("$.maxParticipants").value(100));
    }

    @Test
    @DisplayName("Should return status 404 when calling GET /events/{id} with an invalid ID")
    void getEventDetails_withInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();
        
        when(eventService.getEventDetails(invalidEventId)).thenThrow(new EventNotFoundException("Evento não encontrado."));

        // Act & Assert
        mockMvc.perform(get("/events/{id}", invalidEventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evento não encontrado."));
    }

    @Test
    @DisplayName("Should return status 201 and created event when calling POST /events with valid data")
    void createEvent_withValidData_shouldReturnCreated() throws Exception {
        // Arrange
        EventRequestDTO requestDTO = new EventRequestDTO("Novo Evento", "Descrição", LocalDateTime.now().plusDays(1), 100, "http://image.url", null, "Local", false);
        Event createdEvent = new Event(requestDTO);

        when(eventService.createEvent(any(EventRequestDTO.class))).thenReturn(createdEvent);

        // Act & Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Novo Evento"));
    }

    @Test
    @DisplayName("Should return status 400 when calling POST /events with invalid data")
    void createEvent_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange
        EventRequestDTO requestDTO = new EventRequestDTO("", "Descrição", LocalDateTime.now().plusDays(1), 100, "http://image.url", null, "Local", false);

        // Act & Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("O título não pode estar em branco."));
    }

    @Test
    @DisplayName("Should return status 204 when registering a participant successfully")
    void registerParticipant_withValidData_shouldReturnNoContent() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");
        
        doNothing().when(eventService).registerParticipant(eq(eventId), any(SubscriptionRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/events/{eventId}/register", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return status 404 when registering for a non-existent event")
    void registerParticipant_forNonExistentEvent_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        doThrow(new EventNotFoundException("Evento não encontrado.")).when(eventService).registerParticipant(eq(invalidEventId), any(SubscriptionRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/events/{eventId}/register", invalidEventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evento não encontrado."));
    }

    @Test
    @DisplayName("Should return status 409 when participant is already registered")
    void registerParticipant_whenAlreadyRegistered_shouldReturnConflict() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        doThrow(new SubscriptionAlreadyExistsException("Este participante já está inscrito.")).when(eventService).registerParticipant(eq(eventId), any(SubscriptionRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/events/{eventId}/register", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Este participante já está inscrito."));
    }

    @Test
    @DisplayName("Should return status 400 when registering with an invalid email")
    void registerParticipant_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/events/{eventId}/register", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.participantEmail").value("Formato de e-mail inválido."));
    }

    @Test
    @DisplayName("Should return status 200 and a page of participants when calling GET /events/{eventId}/participants")
    void getRegisteredParticipants_withValidEventId_shouldReturnParticipantsPage() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        RegisteredParticipantDTO participantDTO = new RegisteredParticipantDTO("participant@example.com");
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegisteredParticipantDTO> participantsPage = new PageImpl<>(List.of(participantDTO), pageable, 1);

        when(eventService.getRegisteredParticipants(eq(eventId), any(Pageable.class))).thenReturn(participantsPage);

        // Act & Assert
        mockMvc.perform(get("/events/{eventId}/participants", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].participantEmail").value("participant@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return status 404 when fetching participants for a non-existent event")
    void getRegisteredParticipants_forNonExistentEvent_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();

        when(eventService.getRegisteredParticipants(eq(invalidEventId), any(Pageable.class))).thenThrow(new EventNotFoundException("Evento não encontrado."));

        // Act & Assert
        mockMvc.perform(get("/events/{eventId}/participants", invalidEventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evento não encontrado."));
    }
}
