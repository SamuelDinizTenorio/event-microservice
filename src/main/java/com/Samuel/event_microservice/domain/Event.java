package com.Samuel.event_microservice.domain;

import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.exceptions.EventFullException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "event")
@Table(name = "event")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id;

    private int maxParticipants;
    private int registeredParticipants;
    private String title;
    private LocalDate date;
    private String description;

    public Event(EventRequestDTO eventRequest) {
        if (eventRequest.maxParticipants() <= 0) {
            throw new IllegalArgumentException("O número máximo de participantes deve ser maior que 0.");
        }
        if (eventRequest.date().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data do evento não pode ser no passado.");
        }

        this.id = null; // O ID será gerado pelo JPA
        this.maxParticipants = eventRequest.maxParticipants();
        this.registeredParticipants = 0;
        this.title = eventRequest.title();
        this.date = eventRequest.date();
        this.description = eventRequest.description();
    }

    public void registerParticipant() {
        if (this.registeredParticipants >= this.maxParticipants) {
            throw new EventFullException();
        }
        this.registeredParticipants++;
    }
}
