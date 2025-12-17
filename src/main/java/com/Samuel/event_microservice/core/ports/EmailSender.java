package com.Samuel.event_microservice.core.ports;

import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;

/**
 * Interface (Port) que define o contrato para um serviço de envio de e-mails.
 * <p>
 * Na Arquitetura Limpa/Hexagonal, esta interface atua como uma "Porta de Saída",
 * permitindo que o núcleo da aplicação (casos de uso) envie e-mails sem conhecer
 * os detalhes da implementação (ex: Feign, Kafka, SMTP).
 */
public interface EmailSender {

    /**
     * Envia um e-mail.
     *
     * @param emailRequest O DTO contendo os detalhes do e-mail a ser enviado.
     */
    void sendEmail(EmailRequestDTO emailRequest);
}
