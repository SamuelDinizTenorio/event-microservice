package com.Samuel.event_microservice.core.ports;

import com.Samuel.event_microservice.core.models.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface (Port) que define o contrato para a persistência de Eventos.
 * <p>
 * Esta porta abstrai os detalhes de implementação do banco de dados, permitindo
 * que o núcleo da aplicação opere sem conhecer a tecnologia de persistência (ex: JPA).
 */
public interface EventRepositoryPort {

    Event save(Event event);

    Page<Event> findAll(Pageable pageable);

    Optional<Event> findById(UUID id);

    boolean existsById(UUID id);

    Event getReferenceById(UUID id);

    Page<Event> findUpcomingEvents(LocalDateTime currentDate, Pageable pageable);
}
