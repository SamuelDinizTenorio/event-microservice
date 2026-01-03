package com.Samuel.event_microservice.infrastructure.controller;

import com.Samuel.event_microservice.core.exceptions.EventFullException;
import com.Samuel.event_microservice.core.models.EventStatus;
import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventUpdateDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import com.Samuel.event_microservice.core.usecases.EventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@ActiveProfiles("test")
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

    @Nested
    @DisplayName("GET /events")
    class GetAllEvents {

        @Test
        @DisplayName("Should return status 200 and a complete page of all events when calling GET /events")
        void shouldReturnCompletePageOfEvents() throws Exception {
            // Arrange
            // 1. Defina os parâmetros da REQUISIÇÃO
            String requestedPage = "0";
            String requestedSize = "10";

            // 2. Cria a RESPOSTA esperada do serviço
            UUID eventId = UUID.randomUUID();
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            EventResponseDTO eventDTO = new EventResponseDTO(eventId, "Evento de Teste", "Descrição", start, start.plusHours(2), 100, 10, "http://image.url", "http://event.url", null, true, EventStatus.ACTIVE);
            PageResponseDTO<EventResponseDTO> eventPage = new PageResponseDTO<>(List.of(eventDTO), 0, 10, 1, 1, true);

            when(eventUseCase.getAllEvents(any(Pageable.class)))
                    .thenReturn(eventPage);

            // Act & Assert
            mockMvc.perform(get("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", requestedPage)
                            .param("size", requestedSize))
                    .andExpect(status().isOk())
                    // 3. Verifica os metadados da paginação
                    .andExpect(jsonPath("$.page").value(Integer.parseInt(requestedPage)))
                    .andExpect(jsonPath("$.size").value(Integer.parseInt(requestedSize)))
                    .andExpect(jsonPath("$.total_elements").value(eventPage.totalElements()))
                    .andExpect(jsonPath("$.total_pages").value(eventPage.totalPages()))
                    .andExpect(jsonPath("$.is_last").value(eventPage.isLast()))
                    // 4. Verifica o conteúdo do evento
                    .andExpect(jsonPath("$.content[0].id").value(eventDTO.id().toString()))
                    .andExpect(jsonPath("$.content[0].title").value(eventDTO.title()))
                    .andExpect(jsonPath("$.content[0].description").value(eventDTO.description()))
                    .andExpect(jsonPath("$.content[0].startDateTime").value(eventDTO.startDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content[0].endDateTime").value(eventDTO.endDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content[0].maxParticipants").value(eventDTO.maxParticipants()))
                    .andExpect(jsonPath("$.content[0].registeredParticipants").value(eventDTO.registeredParticipants()))
                    .andExpect(jsonPath("$.content[0].imageUrl").value(eventDTO.imageUrl()))
                    .andExpect(jsonPath("$.content[0].eventUrl").value(eventDTO.eventUrl()))
                    .andExpect(jsonPath("$.content[0].location").value(eventDTO.location()))
                    .andExpect(jsonPath("$.content[0].is_remote").value(eventDTO.is_remote()));
        }

        @Test
        @DisplayName("Should return status 200 and an empty page when no events are found")
        void shouldReturnEmptyPage_whenNoEventsExist() throws Exception {
            // Arrange
            PageResponseDTO<EventResponseDTO> emptyPage = new PageResponseDTO<>(Collections.emptyList(), 0, 10, 0, 0, true);

            when(eventUseCase.getAllEvents(any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.total_elements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /events/upcoming")
    class GetUpcomingEvents {

        @Test
        @DisplayName("Should return status 200 and a complete page of upcoming events")
        void shouldReturnCompletePageOfUpcomingEvents() throws Exception {
            // Arrange
            // 1. Defina os parâmetros da REQUISIÇÃO
            String requestedPage = "0";
            String requestedSize = "10";

            // 2. Cria a RESPOSTA esperada do serviço
            UUID eventId = UUID.randomUUID();
            LocalDateTime start = LocalDateTime.now().plusDays(5);
            EventResponseDTO upcomingEventDTO = new EventResponseDTO(eventId, "Evento Futuro", "Descrição", start, start.plusHours(1), 50, 5, "http://image.url", "http://event.url", null, true, EventStatus.ACTIVE);
            PageResponseDTO<EventResponseDTO> eventPage = new PageResponseDTO<>(List.of(upcomingEventDTO), 0, 10, 1, 1, true);

            when(eventUseCase.getUpcomingEvents(any(Pageable.class)))
                    .thenReturn(eventPage);

            // Act & Assert
            mockMvc.perform(get("/events/upcoming")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", requestedPage)
                            .param("size", requestedSize))
                    .andExpect(status().isOk())
                    // 3. Verifica os metadados da paginação
                    .andExpect(jsonPath("$.page").value(Integer.parseInt(requestedPage)))
                    .andExpect(jsonPath("$.size").value(Integer.parseInt(requestedSize)))
                    .andExpect(jsonPath("$.total_elements").value(eventPage.totalElements()))
                    .andExpect(jsonPath("$.total_pages").value(eventPage.totalPages()))
                    .andExpect(jsonPath("$.is_last").value(eventPage.isLast()))
                    // 4. Verifica o conteúdo do evento
                    .andExpect(jsonPath("$.content[0].id").value(upcomingEventDTO.id().toString()))
                    .andExpect(jsonPath("$.content[0].title").value(upcomingEventDTO.title()))
                    .andExpect(jsonPath("$.content[0].description").value(upcomingEventDTO.description()))
                    .andExpect(jsonPath("$.content[0].startDateTime").value(upcomingEventDTO.startDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content[0].endDateTime").value(upcomingEventDTO.endDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content[0].maxParticipants").value(upcomingEventDTO.maxParticipants()))
                    .andExpect(jsonPath("$.content[0].registeredParticipants").value(upcomingEventDTO.registeredParticipants()))
                    .andExpect(jsonPath("$.content[0].imageUrl").value(upcomingEventDTO.imageUrl()))
                    .andExpect(jsonPath("$.content[0].eventUrl").value(upcomingEventDTO.eventUrl()))
                    .andExpect(jsonPath("$.content[0].location").value(upcomingEventDTO.location()))
                    .andExpect(jsonPath("$.content[0].is_remote").value(upcomingEventDTO.is_remote()));
        }

        @Test
        @DisplayName("Should return status 200 and an empty page when no upcoming events are found")
        void shouldReturnEmptyPage_whenNoUpcomingEventsExist() throws Exception {
            // Arrange
            PageResponseDTO<EventResponseDTO> emptyPage = new PageResponseDTO<>(Collections.emptyList(), 0, 5, 0, 0, true);

            when(eventUseCase.getUpcomingEvents(any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/events/upcoming")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.total_elements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /events/{eventId}")
    class GetEventDetails {

        @Test
        @DisplayName("Should return status 200 and event details when calling GET /events/{id} with a valid ID")
        void shouldReturnEventDetails_whenValidId() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            LocalDateTime start = LocalDateTime.now().plusDays(2);
            EventResponseDTO eventDTO = new EventResponseDTO(eventId, "Evento Detalhado", "Descrição", start, start.plusHours(2), 100, 25, "http://image.url", null, "Local", false, EventStatus.ACTIVE);

            when(eventUseCase.getEventDetails(eventId))
                    .thenReturn(eventDTO);

            // Act & Assert
            mockMvc.perform(get("/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(eventDTO.id().toString()))
                    .andExpect(jsonPath("$.title").value(eventDTO.title()))
                    .andExpect(jsonPath("$.startDateTime").value(eventDTO.startDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.endDateTime").value(eventDTO.endDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.maxParticipants").value(eventDTO.maxParticipants()))
                    .andExpect(jsonPath("$.registeredParticipants").value(eventDTO.registeredParticipants()))
                    .andExpect(jsonPath("$.imageUrl").value(eventDTO.imageUrl()))
                    .andExpect(jsonPath("$.eventUrl").value(eventDTO.eventUrl()))
                    .andExpect(jsonPath("$.location").value(eventDTO.location()))
                    .andExpect(jsonPath("$.is_remote").value(eventDTO.is_remote()));
        }

        @Test
        @DisplayName("Should return status 404 when calling GET /events/{id} with an invalid ID")
        void shouldReturnStatusNotFound_whenInvalidId() throws Exception {
            // Arrange
            UUID invalidEventId = UUID.randomUUID();

            String errorMessage = "Evento não encontrado.";
            when(eventUseCase.getEventDetails(invalidEventId))
                    .thenThrow(new EventNotFoundException(errorMessage));

            // Act & Assert
            mockMvc.perform(get("/events/{eventId}", invalidEventId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("POST /events")
    class CreateEvent {

        @Test
        @DisplayName("Should return status 201 and created event when calling POST /events with valid data")
        void shouldReturnCreatedEvent_whenValidData() throws Exception {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(2);
            EventRequestDTO requestDTO = new EventRequestDTO("Novo Evento", "Descrição longa o suficiente", start, end, 100, "http://image.url", null, "Local", false);
            EventResponseDTO createdEventResponse = new EventResponseDTO(UUID.randomUUID(), "Novo Evento", "Descrição", start, end, 100, 0, "http://image.url", null, "Local", false, EventStatus.ACTIVE);

            when(eventUseCase.createEvent(any(EventRequestDTO.class)))
                    .thenReturn(createdEventResponse);

            // Act & Assert
            mockMvc.perform(post("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", endsWith("/events/" + createdEventResponse.id().toString())))
                    .andExpect(jsonPath("$.id").value(createdEventResponse.id().toString()))
                    .andExpect(jsonPath("$.title").value(createdEventResponse.title()))
                    .andExpect(jsonPath("$.startDateTime").value(createdEventResponse.startDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.endDateTime").value(createdEventResponse.endDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.maxParticipants").value(createdEventResponse.maxParticipants()))
                    .andExpect(jsonPath("$.registeredParticipants").value(createdEventResponse.registeredParticipants()))
                    .andExpect(jsonPath("$.imageUrl").value(createdEventResponse.imageUrl()))
                    .andExpect(jsonPath("$.eventUrl").value(createdEventResponse.eventUrl()))
                    .andExpect(jsonPath("$.location").value(createdEventResponse.location()))
                    .andExpect(jsonPath("$.is_remote").value(createdEventResponse.is_remote()));
        }

        @Test
        @DisplayName("Should return status 400 when title is invalid (blank or too short)")
        void shouldReturnBadRequest_whenTitleIsInvalid() throws Exception {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            // Usa uma string que viola ambas as regras: @NotBlank e @Size
            EventRequestDTO invalidDto = new EventRequestDTO(" ", "Descrição longa o suficiente", start, start.plusHours(1), 100, null, null, "Local", false);

            // Act & Assert
            mockMvc.perform(post("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title", anyOf(
                            is("O título não pode estar em branco."),
                            is("O título deve ter no mínimo 3 caracteres.")
                    )));
        }
        @Test
        @DisplayName("Should return status 400 when description is invalid (blank or too short)")
        void shouldReturnBadRequest_whenDescriptionIsInvalid() throws Exception {
            // Arrange
            LocalDateTime start = LocalDateTime.now().plusDays(1);

            // Usa uma string que viola ambas as regras: @NotBlank e @Size
            EventRequestDTO invalidDto = new EventRequestDTO("Título", " ", start, start.plusHours(1), 100, null, null, "Local", false);

            // Act & Assert
            mockMvc.perform(post("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.description", anyOf(
                            is("A descrição não pode estar em branco."),
                            is("A descrição deve ter no mínimo 10 caracteres.")
                    )));
        }

        // Teste parametrizado para validações de campo único
        static Stream<Arguments> invalidEventProvider() {
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            // DTO, Campo com Erro, Mensagem Esperada
            return Stream.of(
                    arguments(new EventRequestDTO("Título", "Descrição longa", start, start.plusHours(1), 0, null, null, "Local", false), "maxParticipants", "O número máximo de participantes deve ser maior que 0."),
                    arguments(new EventRequestDTO("Título", "Descrição longa", null, start.plusHours(1), 100, null, null, "Local", false), "startDateTime", "A data de início não pode ser nula."),
                    arguments(new EventRequestDTO("Título", "Descrição longa", LocalDateTime.now().minusDays(1), start, 100, null, null, "Local", false), "startDateTime", "A data de início do evento deve ser no futuro."),
                    arguments(new EventRequestDTO("Título", "Descrição longa", start, start.plusHours(1), 100, "url-invalida", null, "Local", false), "imageUrl", "A URL da imagem é inválida.")
                    );
        }

        @ParameterizedTest
        @MethodSource("invalidEventProvider")
        @DisplayName("Should return status 400 for various invalid fields")
        void shouldReturnBadRequest_forVariousInvalidFields(EventRequestDTO invalidDto, String errorField, String expectedMessage) throws Exception {
            // Act & Assert
            mockMvc.perform(post("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors." + errorField).value(expectedMessage));
        }
    }

    @Nested
    @DisplayName("POST /events/{eventId}/cancel")
    class CancelEvent {

        @Test
        @DisplayName("Should return status 200 and success message when cancelling an event successfully")
        void shouldReturnSuccessMessage_whenCancellingExistingEvent() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            String expectedMessage = "Evento cancelado com sucesso!";

            doNothing().when(eventUseCase).cancelEvent(eventId);

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/cancel", eventId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(expectedMessage));
        }

        @Test
        @DisplayName("Should return status 404 when cancelling a non-existent event")
        void shouldReturnStatusNotFound_whenInvalidId() throws Exception {
            // Arrange
            UUID invalidEventId = UUID.randomUUID();
            String errorMessage = "Evento não encontrado.";

            doThrow(new EventNotFoundException(errorMessage))
                    .when(eventUseCase).cancelEvent(invalidEventId);

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/cancel", invalidEventId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("Should return status 400 when business rule for cancellation is violated")
        void shouldReturnStatusBadRequest_whenCancellationRuleIsViolated() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            String errorMessage = "Este evento já está cancelado.";

            // Simula o serviço lançando uma exceção de regra de negócio
            doThrow(new IllegalStateException(errorMessage))
                    .when(eventUseCase).cancelEvent(eventId);

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/cancel", eventId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("POST /events/{eventId}/register")
    class RegisterParticipant {

        @Test
        @DisplayName("Should return status 200 and success message when registering a participant successfully")
        void shouldReturnSuccessMessage_whenRegistrationIsValid() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");
            String expectedMessage = "Inscrição realizada com sucesso!";

            doNothing().when(eventUseCase).registerParticipant(eq(eventId), any(SubscriptionRequestDTO.class));

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/register", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(subscriptionDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(expectedMessage));
        }

        @Test
        @DisplayName("Should return status 404 when registering for a non-existent event")
        void shouldReturnStatusNotFound_whenRegisteringForNonExistentEvent() throws Exception {
            // Arrange
            UUID invalidEventId = UUID.randomUUID();
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");
            String errorMessage = "Evento não encontrado.";

            doThrow(new EventNotFoundException(errorMessage))
                    .when(eventUseCase).registerParticipant(eq(invalidEventId), any(SubscriptionRequestDTO.class));

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/register", invalidEventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(subscriptionDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("Should return status 409 when participant is already registered")
        void shouldReturnStatusConflict_whenParticipantAlreadyRegistered() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");
            String errorMessage = "Este participante já está inscrito.";

            doThrow(new SubscriptionAlreadyExistsException(errorMessage))
                    .when(eventUseCase).registerParticipant(eq(eventId), any(SubscriptionRequestDTO.class));

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/register", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(subscriptionDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("Should return status 400 when registering with an invalid email")
        void shouldReturnStatusBadRequest_whenInvalidEmail() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("invalid-email");
            String errorMessage = "Formato de e-mail inválido.";

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/register", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(subscriptionDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.participantEmail").value(errorMessage));
        }

        @Test
        @DisplayName("Should return status 409 when event is full")
        void shouldReturnStatusConflict_whenEventIsFull() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            SubscriptionRequestDTO subscriptionDTO = new SubscriptionRequestDTO("test@example.com");
            String errorMessage = "O evento já está lotado.";

            // Simula o serviço lançando a exceção de evento lotado
            doThrow(new EventFullException(errorMessage))
                    .when(eventUseCase).registerParticipant(eq(eventId), any(SubscriptionRequestDTO.class));

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/register", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(subscriptionDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("Should return status 400 when request body is missing")
        void shouldReturnBadRequest_whenBodyIsMissing() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(post("/events/{eventId}/register", eventId)
                                    .contentType(MediaType.APPLICATION_JSON)
                            // Envia a requisição sem conteúdo (sem .content(...))
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /events/{eventId}/participants")
    class GetRegisteredParticipants {

        @Test
        @DisplayName("Should return status 200 and a page of participants when calling GET /events/{eventId}/participants")
        void shouldReturnParticipantsPage_whenValidEventId() throws Exception {
            // Arrange
            // 1. Define os parâmetros da REQUISIÇÃO
            String requestedPage = "0";
            String requestedSize = "10";

            // 2. Cria a RESPOSTA esperada do serviço
            UUID eventId = UUID.randomUUID();
            RegisteredParticipantDTO participantDTO = new RegisteredParticipantDTO("participant@example.com");
            PageResponseDTO<RegisteredParticipantDTO> participantsPage = new PageResponseDTO<>(List.of(participantDTO), 0, 10, 1, 1, true);

            when(eventUseCase.getRegisteredParticipants(eq(eventId), any(Pageable.class)))
                    .thenReturn(participantsPage);

            // Act & Assert
            mockMvc.perform(get("/events/{eventId}/participants", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", requestedPage)
                            .param("size", requestedSize))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].participantEmail").value(participantsPage.content().getFirst().participantEmail()))
                    .andExpect(jsonPath("$.page").value(participantsPage.page()))
                    .andExpect(jsonPath("$.size").value(participantsPage.size()))
                    .andExpect(jsonPath("$.total_elements").value(participantsPage.totalElements()))
                    .andExpect(jsonPath("$.total_pages").value(participantsPage.totalPages()))
                    .andExpect(jsonPath("$.is_last").value(participantsPage.isLast()));
        }

        @Test
        @DisplayName("Should return status 404 when fetching participants for a non-existent event")
        void shouldReturnStatusNotFound_whenFetchingParticipantsForNonExistentEvent() throws Exception {
            // Arrange
            UUID invalidEventId = UUID.randomUUID();
            String errorMessage = "Evento não encontrado.";

            when(eventUseCase.getRegisteredParticipants(eq(invalidEventId), any(Pageable.class))).thenThrow(new EventNotFoundException(errorMessage));

            // Act & Assert
            mockMvc.perform(get("/events/{eventId}/participants", invalidEventId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("PATCH /events/{eventId}")
    class UpdateEventTests {

        @Test
        @DisplayName("Should return status 200 and updated event when given valid data")
        void shouldReturnUpdatedEvent_whenDataIsValid() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            EventUpdateDTO updateDTO = new EventUpdateDTO("New Title", "New Description", null, null, 150, null, null, null, null);

            LocalDateTime start = LocalDateTime.now().plusDays(10);
            EventResponseDTO updatedEventResponse = new EventResponseDTO(eventId, "New Title", "New Description", start, start.plusHours(2), 150, 10, "http://image.url", "http://event.url", null, true, EventStatus.ACTIVE);

            when(eventUseCase.updateEvent(eq(eventId), any(EventUpdateDTO.class)))
                    .thenReturn(updatedEventResponse);

            // Act & Assert
            mockMvc.perform(patch("/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(updatedEventResponse.id().toString()))
                    .andExpect(jsonPath("$.title").value(updatedEventResponse.title()))
                    .andExpect(jsonPath("$.description").value(updatedEventResponse.description()))
                    .andExpect(jsonPath("$.startDateTime").value(updatedEventResponse.startDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.endDateTime").value(updatedEventResponse.endDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.maxParticipants").value(updatedEventResponse.maxParticipants()))
                    .andExpect(jsonPath("$.registeredParticipants").value(updatedEventResponse.registeredParticipants()))
                    .andExpect(jsonPath("$.imageUrl").value(updatedEventResponse.imageUrl()))
                    .andExpect(jsonPath("$.eventUrl").value(updatedEventResponse.eventUrl()))
                    .andExpect(jsonPath("$.location").value(updatedEventResponse.location()))
                    .andExpect(jsonPath("$.is_remote").value(updatedEventResponse.is_remote()));
        }

        @Test
        @DisplayName("Should return status 404 when updating a non-existent event")
        void shouldReturnNotFound_whenEventDoesNotExist() throws Exception {
            // Arrange
            UUID invalidEventId = UUID.randomUUID();
            EventUpdateDTO updateDTO = new EventUpdateDTO("New Title", null, null, null, null, null, null, null, null);
            String errorMessage = "Evento com ID " + invalidEventId + " não encontrado.";

            when(eventUseCase.updateEvent(eq(invalidEventId), any(EventUpdateDTO.class)))
                    .thenThrow(new EventNotFoundException(errorMessage));

            // Act & Assert
            mockMvc.perform(patch("/events/{eventId}", invalidEventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("Should return status 400 when a business rule is violated")
        void shouldReturnBadRequest_whenBusinessRuleIsViolated() throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();
            // Tenta diminuir maxParticipants, o que a entidade deve proibir
            EventUpdateDTO invalidUpdateDTO = new EventUpdateDTO(null, null, null, null, 5, null, null, null, null);
            String errorMessage = "O número máximo de participantes não pode ser menor que o número de inscritos.";

            when(eventUseCase.updateEvent(eq(eventId), any(EventUpdateDTO.class)))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // Act & Assert
            mockMvc.perform(patch("/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUpdateDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        // Fonte de dados para campos de atualização inválidos
        static Stream<Arguments> simpleInvalidUpdateProvider() {
            return Stream.of(
                    arguments(new EventUpdateDTO("ab", null, null, null, null, null, null, null, null), "title", "O título deve ter no mínimo 3 caracteres."),
                    arguments(new EventUpdateDTO(null, "Curta", null, null, null, null, null, null, null), "description", "A descrição deve ter no mínimo 10 caracteres."),
                    arguments(new EventUpdateDTO(null, null, LocalDateTime.now().minusDays(1), null, null, null, null, null, null), "startDateTime", "A data de início do evento deve ser no futuro."),
                    arguments(new EventUpdateDTO(null, null, null, null, 0, null, null, null, null), "maxParticipants", "O número máximo de participantes deve ser maior que 0."),
                    arguments(new EventUpdateDTO(null, null, null, null, null, "url-invalida", null, null, null), "imageUrl", "A URL da imagem é inválida."),
                    arguments(new EventUpdateDTO(null, null, null, null, null, null, "url-invalida", null, null), "eventUrl", "A URL do evento é inválida.")
            );
        }

        @ParameterizedTest
        @MethodSource("simpleInvalidUpdateProvider")
        @DisplayName("Should return status 400 for various invalid update fields")
        void shouldReturnBadRequest_forInvalidUpdateFields(EventUpdateDTO invalidDto, String errorField, String expectedMessage) throws Exception {
            // Arrange
            UUID eventId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(patch("/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors." + errorField).value(expectedMessage));
        }
    }
}
