package com.Samuel.event_microservice.core.models;

import com.Samuel.event_microservice.core.data.EventUpdateData;
import com.Samuel.event_microservice.core.exceptions.EventFullException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa a entidade Event no banco de dados.
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
    private boolean isRemote; // Indica se o evento é remoto (online) ou não.

    @Enumerated(EnumType.STRING) // Salva o nome do enum (ACTIVE, CANCELLED) no banco
    private EventStatus status;

    /**
     * Construtor para criar uma instância de Event com validações de domínio.
     *
     * @throws IllegalArgumentException se as regras de negócio forem violadas.
     */
    public Event(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime, int maxParticipants,
                 String imageUrl, String eventUrl, String location, Boolean isRemote, int minDurationInMinutes) {
        
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
        this.status = EventStatus.ACTIVE;
        
        validateBusinessRules(minDurationInMinutes);
    }

    /**
     * Incrementa o contador de participantes registrados no evento.
     *
     * @throws EventFullException se o evento já atingiu a sua capacidade máxima.
     */
    public void registerParticipant() {
        if (this.status != EventStatus.ACTIVE) {
            throw new IllegalStateException("Não é possível se inscrever em um evento que não está ativo.");
        }
        if (this.registeredParticipants >= this.maxParticipants) {
            throw new EventFullException("O evento já está lotado.");
        }
        this.registeredParticipants++;
    }

    /**
     * Cancela o evento, alterando o seu status, se as regras de negócio permitirem.
     * @throws IllegalStateException se o evento já ocorreu ou já está cancelado.
     */
    public void cancel() {
        if (this.endDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível cancelar um evento que já ocorreu.");
        }
        if (this.status == EventStatus.CANCELLED) {
            throw new IllegalStateException("Este evento já está cancelado.");
        }
        this.status = EventStatus.CANCELLED;
    }

    /**
     * Finaliza o evento, alterando o seu status, se as regras de negócio permitirem.
     * @throws IllegalStateException se o evento ainda não ocorreu ou não está ativo.
     */
    public void finish() {
        if (this.status != EventStatus.ACTIVE) {
            throw new IllegalStateException("Apenas eventos ativos podem ser finalizados.");
        }
        if (this.endDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Não é possível finalizar um evento que ainda não terminou.");
        }
        this.status = EventStatus.FINISHED;
    }

    /**
     * Atualiza os detalhes de um evento com base nos dados de um objeto de dados do domínio.
     * Apenas os campos não nulos no objeto de dados são usados para a atualização.
     *
     * @param data O objeto {@link EventUpdateData} com os novos dados.
     * @param minDurationInMinutes A duração mínima configurada para um evento.
     * @throws IllegalStateException se o evento não estiver ativo.
     * @throws IllegalArgumentException se as novas regras de negócio forem violadas.
     */
    public void updateDetails(EventUpdateData data, int minDurationInMinutes) {
        if (this.status != EventStatus.ACTIVE) {
            throw new IllegalStateException("Apenas eventos ativos podem ser atualizados.");
        }

        if (data.title() != null && !data.title().isBlank()) this.title = data.title();
        if (data.description() != null) this.description = data.description();
        if (data.maxParticipants() != null) {
            if (data.maxParticipants() < this.registeredParticipants) {
                throw new IllegalArgumentException("O número máximo de participantes não pode ser menor que o número de inscritos (" + this.registeredParticipants + ").");
            }
            this.maxParticipants = data.maxParticipants();
        }
        if (data.startDateTime() != null) this.startDateTime = data.startDateTime();
        if (data.endDateTime() != null) this.endDateTime = data.endDateTime();
        if (data.imageUrl() != null) this.imageUrl = data.imageUrl();
        if (data.eventUrl() != null) this.eventUrl = data.eventUrl();
        if (data.location() != null) this.location = data.location();
        if (data.remote() != null) this.isRemote = data.remote();

        validateBusinessRules(minDurationInMinutes);
    }

    private void validateBusinessRules(int minDurationInMinutes) {
        // Validações de consistência de dados
        if (title == null || title.trim().length() < 3) {
            throw new IllegalArgumentException("O título deve ter no mínimo 3 caracteres.");
        }
        if (description == null || description.trim().length() < 10) {
            throw new IllegalArgumentException("A descrição deve ter no mínimo 10 caracteres.");
        }
        if (this.maxParticipants <= 0) {
            throw new IllegalArgumentException("O número máximo de participantes deve ser maior que 0.");
        }
        if (this.startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data de início do evento deve ser no futuro.");
        }
        if (this.endDateTime.isBefore(this.startDateTime)) {
            throw new IllegalArgumentException("A data de encerramento deve ser posterior à data de início.");
        }
        if (this.endDateTime.isBefore(this.startDateTime.plusMinutes(minDurationInMinutes))) {
            throw new IllegalArgumentException("O evento deve ter uma duração de pelo menos " + minDurationInMinutes + " minutos.");
        }
        
        // Validações de consistência entre 'is_remote', 'location' e 'eventUrl'
        if (this.isRemote) {
            if (this.location != null && !this.location.isBlank()) {
                throw new IllegalArgumentException("Um evento remoto não pode ter uma localização física.");
            }
            if (this.eventUrl == null || this.eventUrl.isBlank()) {
                throw new IllegalArgumentException("Um evento remoto deve ter uma URL de evento.");
            }
        }
        if (!this.isRemote) {
            if (this.location == null || this.location.isBlank()) {
                throw new IllegalArgumentException("Um evento presencial deve ter uma localização física.");
            }
            if (this.eventUrl != null && !this.eventUrl.isBlank()) {
                throw new IllegalArgumentException("Um evento presencial não deve ter uma URL de evento.");
            }
        }
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
