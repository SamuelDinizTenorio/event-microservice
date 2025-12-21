package com.Samuel.event_microservice.core.ports;

import com.Samuel.event_microservice.core.models.Event;

/**
 * Interface (Port) que define o contrato para o envio de notificações relacionadas a eventos.
 * <p>
 * Esta porta abstrai a implementação concreta do mecanismo de notificação (e-mail, SMS, etc.).
 */
public interface EventNotificationPort {

    /**
     * Notifica todos os participantes de um evento sobre o seu cancelamento.
     *
     * @param event O evento que foi cancelado.
     */
    void notifyParticipantsOfCancellation(Event event);

    /**
     * Envia um e-mail de confirmação de inscrição para um participante.
     *
     * @param event O evento ao qual o participante se inscreveu.
     * @param participantEmail O e-mail do participante.
     */
    void sendRegistrationConfirmation(Event event, String participantEmail);
}
