package com.Samuel.event_microservice.domain;

import com.Samuel.event_microservice.dto.EventRequestDTO;
import com.Samuel.event_microservice.exceptions.EventFullException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa a entidade Event no banco de dados.
 * Contém todas as informações sobre um evento específico.
 */
@Entity(name = "event")
@Table(name = "event")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id; // Identificador único do evento (UUID).

    private int maxParticipants; // O número máximo de participantes permitidos no evento.
    private int registeredParticipants; // O número de participantes atualmente registrados no evento.
    private String title; // O título do evento.
    private LocalDateTime date; // A data e hora em que o evento ocorrerá.
    private String description; // Uma descrição detalhada do evento.
    private String imageUrl; // A URL de uma imagem de banner para o evento.
    private String eventUrl; // A URL para acessar o evento, caso seja remoto.
    private String location; // O endereço físico do evento, caso seja presencial.
    private Boolean isRemote; // Indica se o evento é remoto (online) ou não.

    /**
     * Construtor que cria uma nova instância de Event a partir de um DTO de requisição.
     * Realiza validações de regras de negócio antes de criar o objeto.
     *
     * @param eventRequest O DTO contendo os dados do novo evento.
     * @throws IllegalArgumentException se a data do evento for no passado ou o número máximo
     *                                  de participantes for menor ou igual a zero.
     */
    public Event(EventRequestDTO eventRequest) {
        if (eventRequest.maxParticipants() <= 0) {
            throw new IllegalArgumentException("O número máximo de participantes deve ser maior que 0.");
        }
        if (eventRequest.date().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data do evento não pode ser no passado.");
        }

        this.id = null; // O ID será gerado pelo JPA
        this.maxParticipants = eventRequest.maxParticipants();
        this.registeredParticipants = 0;
        this.title = eventRequest.title();
        this.date = eventRequest.date();
        this.description = eventRequest.description();
        this.imageUrl = eventRequest.imageUrl();
        this.eventUrl = eventRequest.eventUrl();
        this.location = eventRequest.location();
        this.isRemote = eventRequest.isRemote();
    }

    /**
     * Incrementa o contador de participantes registrados no evento.
     *
     * @throws EventFullException se o evento já atingiu sua capacidade máxima.
     */
    public void registerParticipant() {
        if (this.registeredParticipants >= this.maxParticipants) {
            throw new EventFullException();
        }
        this.registeredParticipants++;
    }
}
