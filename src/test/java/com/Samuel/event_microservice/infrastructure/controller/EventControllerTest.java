package com.Samuel.event_microservice.infrastructure.controller;

import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.core.usecases.EventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventUseCase eventUseCase;

    @BeforeEach
    void setUp() {
        // Garante que o ObjectMapper usado nos testes entenda os tipos do Java 8 (LocalDateTime)
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should return status 200 and a page of all events when calling GET /events")
    void getAllEvents_shouldReturnPageOfEvents() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        EventResponseDTO eventDTO = new EventResponseDTO(eventId, "Evento de Teste", "Descrição", start, start.plusHours(2), 100, 10, "http://image.url", "http://event.url", null, true);
        PageResponseDTO<EventResponseDTO> eventPage = new PageResponseDTO<>(List.of(eventDTO), 0, 10, 1, 1, true);

        when(eventUseCase.getAllEvents(any(Pageable.class))).thenReturn(eventPage);

        // Act & Assert
        mockMvc.perform(get("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Evento de Teste"))
                .andExpect(jsonPath("$.total_elements").value(1));
    }

    @Test
    @DisplayName("Should return status 200 and a page of upcoming events when calling GET /events/upcoming")
    void getUpcomingEvents_shouldReturnPageOfUpcomingEvents() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        EventResponseDTO upcomingEventDTO = new EventResponseDTO(eventId, "Evento Futuro", "Descrição", start, start.plusHours(1), 50, 5, "http://image.url", "http://event.url", null, true);
        PageResponseDTO<EventResponseDTO> eventPage = new PageResponseDTO<>(List.of(upcomingEventDTO), 0, 5, 1, 1, true);

        when(eventUseCase.getUpcomingEvents(any(Pageable.class))).thenReturn(eventPage);

        // Act & Assert
        mockMvc.perform(get("/events/upcoming")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Evento Futuro"))
                .andExpect(jsonPath("$.total_elements").value(1));
    }

    @Test
    @DisplayName("Should return status 200 and event details when calling GET /events/{id} with a valid ID")
    void getEventDetails_withValidId_shouldReturnEventDetails() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        EventResponseDTO eventDTO = new EventResponseDTO(eventId, "Evento Detalhado", "Descrição", start, start.plusHours(2), 100, 25, "http://image.url", null, "Local", false);

        when(eventUseCase.getEventDetails(eventId)).thenReturn(eventDTO);

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

        when(eventUseCase.getEventDetails(invalidEventId)).thenThrow(new EventNotFoundException("Evento não encontrado."));

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
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        EventRequestDTO requestDTO = new EventRequestDTO("Novo Evento", "Descrição", start, end, 100, "http://image.url", null, "Local", false);
        EventResponseDTO createdEventResponse = new EventResponseDTO(UUID.randomUUID(), "Novo Evento", "Descrição", start, end, 100, 0, "http://image.url", null, "Local", false);

        when(eventUseCase.createEvent(any(EventRequestDTO.class))).thenReturn(createdEventResponse);

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
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        EventRequestDTO requestDTO = new EventRequestDTO("", "Descrição", start, start.plusHours(1), 100, "http://image.url", null, "Local", false);

        // Act & Assert
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("O título não pode estar em branco."));
    }

    @Test
    @DisplayName("Should return status 200 and success message when registering a participant successfully")
    void registerParticipant_withValidData_shouldReturnOk() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        doNothing().when(eventUseCase).registerParticipant(eq(eventId), any(SubscriptionRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/events/{eventId}/register", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscrição realizada com sucesso!"));
    }

    @Test
    @DisplayName("Should return status 404 when registering for a non-existent event")
    void registerParticipant_forNonExistentEvent_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();
        SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");

        doThrow(new EventNotFoundException("Evento não encontrado.")).when(eventUseCase).registerParticipant(eq(invalidEventId), any(SubscriptionRequestDTO.class));

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

        doThrow(new SubscriptionAlreadyExistsException("Este participante já está inscrito.")).when(eventUseCase).registerParticipant(eq(eventId), any(SubscriptionRequestDTO.class));

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
        PageResponseDTO<RegisteredParticipantDTO> participantsPage = new PageResponseDTO<>(List.of(participantDTO), 0, 10, 1, 1, true);

        when(eventUseCase.getRegisteredParticipants(eq(eventId), any(Pageable.class))).thenReturn(participantsPage);

        // Act & Assert
        mockMvc.perform(get("/events/{eventId}/participants", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].participantEmail").value("participant@example.com"))
                .andExpect(jsonPath("$.total_elements").value(1));
    }

    @Test
    @DisplayName("Should return status 404 when fetching participants for a non-existent event")
    void getRegisteredParticipants_forNonExistentEvent_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID invalidEventId = UUID.randomUUID();

        when(eventUseCase.getRegisteredParticipants(eq(invalidEventId), any(Pageable.class))).thenThrow(new EventNotFoundException("Evento não encontrado."));

        // Act & Assert
        mockMvc.perform(get("/events/{eventId}/participants", invalidEventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Evento não encontrado."));
    }
}
