package com.Samuel.event_microservice.infrastructure.adapters;

import com.Samuel.event_microservice.infrastructure.clients.EmailServiceClient;
import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeignEmailSenderAdapterTest {

    @InjectMocks
    private FeignEmailSenderAdapter feignEmailSenderAdapter;

    @Mock
    private EmailServiceClient emailServiceClient;

    @Test
    @DisplayName("Should call Feign client with the correct email data")
    void sendEmail_shouldCallFeignClientWithCorrectData() {
        // Arrange
        EmailRequestDTO originalEmailRequest = new EmailRequestDTO("test@example.com", "Assunto", "Corpo do e-mail");
        ArgumentCaptor<EmailRequestDTO> emailCaptor = ArgumentCaptor.forClass(EmailRequestDTO.class);

        // Act
        feignEmailSenderAdapter.sendEmail(originalEmailRequest);

        // Assert
        // 1. Verifica se o método do cliente Feign foi chamado uma vez e captura o argumento
        verify(emailServiceClient).sendEmail(emailCaptor.capture());

        // 2. Pega o DTO que foi capturado
        EmailRequestDTO capturedEmail = emailCaptor.getValue();

        // 3. Usa AssertJ para verificar o conteúdo do DTO capturado
        assertThat(capturedEmail).isNotNull();
        assertThat(capturedEmail.to()).isEqualTo(originalEmailRequest.to());
        assertThat(capturedEmail.subject()).isEqualTo(originalEmailRequest.subject());
        assertThat(capturedEmail.body()).isEqualTo(originalEmailRequest.body());
    }
}