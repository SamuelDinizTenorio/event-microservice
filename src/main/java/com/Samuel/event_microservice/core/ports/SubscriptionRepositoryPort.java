package com.Samuel.event_microservice.core.ports;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface (Port) que define o contrato para a persistência de Inscrições.
 * <p>
 * Esta porta abstrai os detalhes de implementação do banco de dados.
 */
public interface SubscriptionRepositoryPort {

    /**
     * Salva uma nova inscrição ou atualiza uma existente.
     *
     * @param subscription A inscrição a ser salva.
     * @return A inscrição salva, que pode incluir um ID gerado.
     */
    Subscription save(Subscription subscription);

    /**
     * Busca uma página de inscrições para um determinado evento.
     *
     * @param event O evento para o qual as inscrições serão buscadas.
     * @param pageable Objeto de paginação para controlar o tamanho e a ordenação da página.
     * @return Uma página de inscrições para o evento.
     */
    Page<Subscription> findByEvent(Event event, Pageable pageable);

    /**
     * Busca todas as inscrições para um determinado evento, sem paginação.
     *
     * @param event O evento para o qual as inscrições serão buscadas.
     * @return Uma lista de todas as inscrições para o evento.
     */
    List<Subscription> findByEvent(Event event);

    /**
     * Busca uma inscrição específica para um evento e e-mail de participante.
     *
     * @param event O evento da inscrição.
     * @param participantEmail O e-mail do participante.
     * @return Um Optional contendo a inscrição, se encontrada.
     */
    Optional<Subscription> findByEventAndParticipantEmail(Event event, String participantEmail);
}
