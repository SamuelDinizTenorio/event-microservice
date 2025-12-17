package com.Samuel.event_microservice.infrastructure.adapters;

import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import com.Samuel.event_microservice.core.ports.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa a porta {@link EmailSender} usando um cliente Feign.
 * <p>
 * Esta classe traduz a chamada de negócio para enviar um e-mail em uma
 * requisição HTTP para o microsserviço de e-mail.
 */
@Component
@RequiredArgsConstructor
public class FeignEmailSenderAdapter implements EmailSender {

    private final EmailServiceClient emailServiceClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendEmail(EmailRequestDTO emailRequest) {
        this.emailServiceClient.sendEmail(emailRequest);
    }
}
