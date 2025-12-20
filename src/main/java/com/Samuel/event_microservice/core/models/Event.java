package com.Samuel.event_microservice.core.models;

import com.Samuel.event_microservice.core.exceptions.EventFullException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
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
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private final UUID id; // Identificador único do evento (UUID).

    private int maxParticipants; // O número máximo de participantes permitidos no evento.
    private int registeredParticipants; // O número de participantes atualmente registrados no evento.
    private String title; // O título do evento.
    private String description; // Uma descrição detalhada do evento.
    private LocalDateTime startDateTime; // A data e hora de início do evento.
    private LocalDateTime endDateTime; // A data e hora de encerramento do evento.
    private String imageUrl; // A URL de uma imagem de banner para o evento.
    private String eventUrl; // A URL para acessar o evento, caso seja remoto.
    private String location; // O endereço físico do evento, caso seja presencial.
    private Boolean isRemote; // Indica se o evento é remoto (online) ou não.

    /**
     * Construtor para criar uma nova instância de Event com validações de domínio.
     *
     * @param title O título do evento.
     * @param description A descrição do evento.
     * @param startDateTime A data e hora de início do evento.
     * @param endDateTime A data e hora de encerramento do evento.
     * @param maxParticipants O número máximo de participantes.
     * @param imageUrl A URL da imagem do evento.
     * @param eventUrl A URL do evento (se remoto).
     * @param location A localização do evento (se presencial).
     * @param isRemote Indica se o evento é remoto.
     * @throws IllegalArgumentException se as datas forem inválidas ou maxParticipants for <= 0.
     */
    public Event(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime, int maxParticipants,
                 String imageUrl, String eventUrl, String location, Boolean isRemote) {
        
        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("O número máximo de participantes deve ser maior que 0.");
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data de início do evento não pode ser no passado.");
        }
        if (endDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("A data de encerramento deve ser posterior à data de início.");
        }
        // Adiciona uma validação de duração mínima (ex: 15 minutos)
        if (endDateTime.isBefore(startDateTime.plusMinutes(15))) {
            throw new IllegalArgumentException("O evento deve ter uma duração de pelo menos 15 minutos.");
        }

        this.id = null; // O ID será gerado pelo JPA
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        // Compara pela identidade (ID) apenas se o ID não for nulo.
        // Se o ID for nulo, duas entidades são consideradas diferentes a menos que sejam a mesma instância.
        return id != null && Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        // Retorna um valor constante para garantir que o hashCode não mude
        // quando o ID é gerado, o que é crucial para o funcionamento correto em coleções como HashSet.
        return getClass().hashCode();
    }
}
