package com.Samuel.event_microservice.core.models;

import com.Samuel.event_microservice.core.exceptions.EventFullException;
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
@AllArgsConstructor // Mantido para o @Builder
@Builder // Mantido para facilitar a criação de objetos em testes
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
     * Construtor para criar uma nova instância de Event com validações de domínio.
     * Este construtor recebe os dados brutos necessários para a criação de um evento,
     * garantindo que a entidade seja criada em um estado válido.
     *
     * @param title O título do evento.
     * @param description A descrição do evento.
     * @param date A data e hora do evento.
     * @param maxParticipants O número máximo de participantes.
     * @param imageUrl A URL da imagem do evento.
     * @param eventUrl A URL do evento (se remoto).
     * @param location A localização do evento (se presencial).
     * @param isRemote Indica se o evento é remoto.
     * @throws IllegalArgumentException se a data for no passado ou maxParticipants for <= 0.
     */
    public Event(String title, String description, LocalDateTime date, int maxParticipants,
                 String imageUrl, String eventUrl, String location, Boolean isRemote) {
        
        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("O número máximo de participantes deve ser maior que 0.");
        }
        if (date.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data do evento não pode ser no passado.");
        }

        this.id = null; // O ID será gerado pelo JPA
        this.title = title;
        this.description = description;
        this.date = date;
        this.maxParticipants = maxParticipants;
        this.registeredParticipants = 0; // Sempre inicia com 0
        this.imageUrl = imageUrl;
        this.eventUrl = eventUrl;
        this.location = location;
        this.isRemote = isRemote;
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
