package com.Samuel.event_microservice.repositories;

import com.Samuel.event_microservice.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repositório para operações de acesso a dados da entidade {@link Event}.
 * Estende {@link JpaRepository} para fornecer métodos CRUD básicos e funcionalidades
 * de paginação e ordenação para a entidade Event, usando {@link UUID} como tipo
 * do identificador.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
     * Busca uma página de eventos que ocorrerão após a data e hora atuais.
     *
     * @param currentDate A data e hora atual, para filtrar apenas eventos futuros.
     * @param pageable    O objeto de paginação para controlar o tamanho da página e o número da página.
     * @return Uma {@link Page} de {@link Event} futuros.
     */
    @Query("SELECT e FROM event e WHERE e.date > :currentDate")
    Page<Event> findUpcomingEvents(@Param("currentDate") LocalDateTime currentDate, Pageable pageable);
}
