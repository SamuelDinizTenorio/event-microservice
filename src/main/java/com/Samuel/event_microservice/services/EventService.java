package com.Samuel.event_microservice.services;

import com.Samuel.event_microservice.domain.Event;
import com.Samuel.event_microservice.domain.Subscription;
import com.Samuel.event_microservice.dto.EmailRequestDTO;
import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.dto.EventResponseDTO;
import com.Samuel.event_microservice.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.repositories.EventRepository;
import com.Samuel.event_microservice.repositories.SubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailServiceClient emailServiceClient;

    public Page<EventResponseDTO> getAllEvents(Pageable pageable) {
        Page<Event> eventPage = eventRepository.findAll(pageable);
        return eventPage.map(EventResponseDTO::new);
    }

    public Page<EventResponseDTO> getUpcomingEvents(Pageable pageable) {
        Page<Event> eventPage = eventRepository.findUpcomingEvents(LocalDate.now(), pageable);
        return eventPage.map(EventResponseDTO::new);
    }

    public EventResponseDTO getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Evento com ID " + id + " não encontrado."));
        return new EventResponseDTO(event);
    }

    @Transactional
    public Event createEvent(EventRequestDTO eventRequest) {
        Event newEvent = new Event(eventRequest);
        return eventRepository.save(newEvent);
    }

    @Transactional
    public void registerParticipant(UUID eventId, String participantEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Evento com ID " + eventId + " não encontrado."));

        // A validação de lotação e o incremento agora estão dentro do método da entidade
        event.registerParticipant(); // Lança EventFullException se estiver lotado

        Subscription subscription = new Subscription(event, participantEmail);
        subscriptionRepository.save(subscription);

        eventRepository.save(event); // Salva o evento com o número de participantes atualizado

        EmailRequestDTO emailRequest = new EmailRequestDTO(
                participantEmail,
                "Confirmação de Inscrição",
                "Você foi inscrito no evento com sucesso!");

        emailServiceClient.sendEmail(emailRequest);
    }
}
