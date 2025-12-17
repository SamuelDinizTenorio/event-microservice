package com.Samuel.event_microservice.core.ports;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Interface (Port) que define o contrato para a persistência de Inscrições.
 * <p>
 * Esta porta abstrai os detalhes de implementação do banco de dados.
 */
public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Page<Subscription> findByEvent(Event event, Pageable pageable);

    Optional<Subscription> findByEventAndParticipantEmail(Event event, String participantEmail);
}
