package com.Samuel.event_microservice.infrastructure.adapters;

import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeignEmailSenderAdapterTest {

    @InjectMocks
    private FeignEmailSenderAdapter feignEmailSenderAdapter;

    @Mock
    private EmailServiceClient emailServiceClient;

    @Test
    @DisplayName("Should call the Feign client when sendEmail is invoked")
    void sendEmail_shouldCallFeignClient() {
        // Arrange
        EmailRequestDTO emailRequest = new EmailRequestDTO("test@example.com", "Assunto", "Corpo do e-mail");

        // Act
        feignEmailSenderAdapter.sendEmail(emailRequest);

        // Assert
        // Verifica se o método sendEmail do cliente Feign foi chamado exatamente uma vez
        // com o objeto de requisição de e-mail correto.
        verify(emailServiceClient, times(1)).sendEmail(emailRequest);
    }
}
