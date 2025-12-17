package com.Samuel.event_microservice.infrastructure.repositories;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para a entidade {@link Subscription}.
 * Esta interface atua como um Adaptador de Persistência, implementando a
 * {@link SubscriptionRepositoryPort} e usando o Spring Data JPA para interagir com o banco de dados.
 */
@Repository
public interface JpaSubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryPort {

    /**
     * {@inheritDoc}
     * <p>
     * A implementação deste método é gerada pelo Spring Data JPA com base no nome do método.
     */
    @Override
    Page<Subscription> findByEvent(Event event, Pageable pageable);

    /**
     * {@inheritDoc}
     * <p>
     * A implementação deste método é gerada pelo Spring Data JPA com base no nome do método.
     */
    @Override
    Optional<Subscription> findByEventAndParticipantEmail(Event event, String participantEmail);
}
