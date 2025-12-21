package com.Samuel.event_microservice.core.usecases;

import com.Samuel.event_microservice.infrastructure.dto.PageResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventRequestDTO;
import com.Samuel.event_microservice.infrastructure.dto.event.EventResponseDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.RegisteredParticipantDTO;
import com.Samuel.event_microservice.infrastructure.dto.subscription.SubscriptionRequestDTO;
import com.Samuel.event_microservice.core.exceptions.EventFullException;
import com.Samuel.event_microservice.core.exceptions.EventNotFoundException;
import com.Samuel.event_microservice.core.exceptions.SubscriptionAlreadyExistsException;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Interface que define os casos de uso (regras de negócio da aplicação) para a entidade Event.
 * Serve como um contrato para a camada de serviço, aplicando o Princípio da Inversão de Dependência.
 */
public interface EventUseCase {

    /**
     * Cria um evento com base nos dados fornecidos.
     *
     * @param eventRequest DTO com os dados do evento a ser criado.
     * @return Um DTO com os detalhes do evento que foi criado.
     */
    EventResponseDTO createEvent(EventRequestDTO eventRequest);

    /**
     * Retorna uma página de todos os eventos, incluindo futuros e passados.
     *
     * @param pageable Objeto de paginação para controlar o tamanho e a ordenação da página.
     * @return Um DTO de resposta paginada contendo {@link EventResponseDTO}.
     */
    PageResponseDTO<EventResponseDTO> getAllEvents(Pageable pageable);

    /**
     * Retorna uma página de eventos que ainda não ocorreram.
     *
     * @param pageable Objeto de paginação para controlar o tamanho e a ordenação da página.
     * @return Um DTO de resposta paginada contendo {@link EventResponseDTO} dos eventos futuros.
     */
    PageResponseDTO<EventResponseDTO> getUpcomingEvents(Pageable pageable);

    /**
     * Busca os detalhes de um evento específico pelo seu ID.
     *
     * @param id O UUID do evento a ser buscado.
     * @return Um DTO com os detalhes do evento.
     * @throws EventNotFoundException se o evento com o ID fornecido não for encontrado.
     */
    EventResponseDTO getEventDetails(UUID id);

    /**
     * Cancela um evento, marcando seu status como CANCELLED.
     *
     * @param eventId O UUID do evento a ser cancelado.
     */
    void cancelEvent(UUID eventId);

    /**
     * Registra um participante em um evento específico.
     *
     * @param eventId O UUID do evento no qual o participante será registrado.
     * @param subscriptionRequest DTO contendo o e-mail do participante.
     * @throws EventNotFoundException se o evento com o ID fornecido não for encontrado.
     * @throws SubscriptionAlreadyExistsException se o participante já estiver inscrito no evento.
     * @throws EventFullException se o evento já estiver lotado.
     */
    void registerParticipant(UUID eventId, SubscriptionRequestDTO subscriptionRequest);

    /**
     * Retorna uma página de participantes registrados em um evento específico.
     *
     * @param eventId O UUID do evento.
     * @param pageable Objeto de paginação para controlar o tamanho e a ordenação da página.
     * @return Um DTO de resposta paginada contendo {@link RegisteredParticipantDTO} com os e-mails dos participantes.
     * @throws EventNotFoundException se o evento com o ID fornecido não for encontrado.
     */
    PageResponseDTO<RegisteredParticipantDTO> getRegisteredParticipants(UUID eventId, Pageable pageable);
}
