package com.Samuel.event_microservice.dto;

/**
 * DTO para encapsular os dados necessários para enviar um e-mail.
 * Usado para comunicação com o microsserviço de e-mail.
 *
 * @param to      O endereço de e-mail do destinatário.
 * @param subject O assunto do e-mail.
 * @param body    O corpo do e-mail (conteúdo).
 */
public record EmailRequestDTO(
        String to,
        String subject,
        String body
) {
}
