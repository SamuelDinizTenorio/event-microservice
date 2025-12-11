package com.Samuel.event_microservice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "subscription")
@Table(name = "subscription")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@EqualsAndHashCode(of = "id")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @ManyToOne
    private final Event event;

    private final String participantEmail;

    public Subscription(Event event, String participantEmail) {
        if (event == null) {
            throw new IllegalArgumentException("O evento não pode ser nulo.");
        }
        if (participantEmail == null || participantEmail.isBlank()) {
            throw new IllegalArgumentException("O email do participante não pode ser nulo ou vazio.");
        }

        this.id = null; // O ID será gerado pelo JPA
        this.event = event;
        this.participantEmail = participantEmail;
    }
}
