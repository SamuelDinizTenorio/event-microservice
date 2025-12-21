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

    /**
     * Salva um novo evento ou atualiza um existente.
     *
     * @param event O evento a ser salvo.
     * @return O evento salvo, que pode incluir um ID gerado.
     */
    Event save(Event event);

    /**
     * Busca uma página de todos os eventos.
     *
     * @param pageable Objeto de paginação para controlar o tamanho e a ordenação da página.
     * @return Uma página de eventos.
     */
    Page<Event> findAll(Pageable pageable);

    /**
     * Busca um evento pelo seu ID.
     *
     * @param id O UUID do evento.
     * @return Um Optional contendo o evento, se encontrado.
     */
    Optional<Event> findById(UUID id);

    /**
     * Verifica se um evento com o ID especificado existe.
     *
     * @param id O UUID do evento.
     * @return true se o evento existir, false caso contrário.
     */
    boolean existsById(UUID id);

    /**
     * Obtém uma referência para um evento pelo seu ID sem carregá-lo completamente.
     * Útil para relacionamentos.
     *
     * @param id O UUID do evento.
     * @return Uma referência (proxy) para o evento.
     */
    Event getReferenceById(UUID id);

    /**
     * Busca uma página de eventos futuros a partir de uma data de referência.
     *
     * @param currentDate A data e hora a partir da qual os eventos são considerados futuros.
     * @param pageable Objeto de paginação para controlar o tamanho e a ordenação da página.
     * @return Uma página de eventos futuros.
     */
    Page<Event> findUpcomingEvents(LocalDateTime currentDate, Pageable pageable);
}
