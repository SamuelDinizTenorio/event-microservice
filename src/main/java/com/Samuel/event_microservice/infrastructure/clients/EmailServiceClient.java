package com.Samuel.event_microservice.infrastructure.clients;

import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente Feign para comunicação com o microsserviço de e-mail.
 * <p>
 * Esta interface é usada para fazer chamadas HTTP declarativas para o serviço de notificação,
 * permitindo o envio de e-mails de forma desacoplada.
 */
@FeignClient(name = "email-service", url = "${clients.email-service.url}")
public interface EmailServiceClient {

    /**
     * Envia uma requisição para o microsserviço de e-mail para despachar um e-mail.
     *
     * @param emailRequest O DTO contendo os detalhes do e-mail a ser enviado (destinatário, assunto, corpo).
     */
    @PostMapping("/send")
    void sendEmail(@RequestBody EmailRequestDTO emailRequest);
}
