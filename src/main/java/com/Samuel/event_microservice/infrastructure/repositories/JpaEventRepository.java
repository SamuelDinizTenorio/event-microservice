package com.Samuel.event_microservice.infrastructure.repositories;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repositório JPA para a entidade {@link Event}.
 * Esta interface atua como um Adaptador de Persistência, implementando a
 * {@link EventRepositoryPort} e usando o Spring Data JPA para interagir com o banco de dados.
 */
@Repository
public interface JpaEventRepository extends JpaRepository<Event, UUID>, EventRepositoryPort {

    /**
     * {@inheritDoc}
     * <p>
     * A implementação deste método é fornecida pelo Spring Data JPA.
     */
    @Override
    @Query("SELECT e FROM event e WHERE e.startDateTime > :currentDate")
    Page<Event> findUpcomingEvents(@Param("currentDate") LocalDateTime currentDate, Pageable pageable);
}
