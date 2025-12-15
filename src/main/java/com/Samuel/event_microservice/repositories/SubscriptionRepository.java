package com.Samuel.event_microservice.repositories;

import com.Samuel.event_microservice.domain.Event;
import com.Samuel.event_microservice.domain.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para operações de acesso a dados da entidade {@link Subscription}.
 * Estende {@link JpaRepository} para fornecer métodos CRUD básicos e funcionalidades
 * de paginação e ordenação para a entidade Subscription, usando {@link Long} como
 * tipo do identificador.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Busca uma página de inscrições associadas a um evento específico.
     *
     * @param event O evento para o qual as inscrições devem ser encontradas.
     * @param pageable O objeto de paginação para controlar o tamanho e o número da página.
     * @return Uma {@link Page} de {@link Subscription} para o evento fornecido.
     */
    Page<Subscription> findByEvent(Event event, Pageable pageable);

    /**
     * Busca uma inscrição específica com base no evento e no e-mail do participante.
     *
     * @param event O evento.
     * @param participantEmail O e-mail do participante.
     * @return Um {@link Optional} contendo a inscrição, se encontrada.
     */
    Optional<Subscription> findByEventAndParticipantEmail(Event event, String participantEmail);
}
