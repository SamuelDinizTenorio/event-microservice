package com.Samuel.event_microservice.core.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa a entidade Subscription (inscrição) no banco de dados.
 * Registra a participação de um usuário em um evento.
 */
@Entity(name = "subscription")
@Table(name = "subscription")
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id; // Identificador único da inscrição.

    @ManyToOne
    private final Event event; // O evento ao qual esta inscrição pertence.

    private final String participantEmail; // O e-mail do participante inscrito.

    private final LocalDateTime createdAt; // A data e hora em que a inscrição foi criada.

    /**
     * Construtor que cria uma nova instância de Subscription.
     * Realiza validações básicas antes de criar o objeto.
     *
     * @param event O evento ao qual o participante está se inscrevendo.
     * @param participantEmail O e-mail do participante.
     * @throws IllegalArgumentException se o evento for nulo ou o e-mail do participante for nulo/vazio.
     */
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
        this.createdAt = LocalDateTime.now(); // Define a data de criação
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        // Compara pela identidade (ID) apenas se o ID não for nulo.
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // Retorna um valor constante para garantir que o hashCode não mude.
        return getClass().hashCode();
    }
}
