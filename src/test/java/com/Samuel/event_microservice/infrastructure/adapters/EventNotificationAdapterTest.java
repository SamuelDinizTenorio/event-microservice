package com.Samuel.event_microservice.infrastructure.adapters;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.core.ports.EmailSender;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventNotificationAdapterTest {

    @InjectMocks
    private EventNotificationAdapter eventNotificationAdapter;

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private EmailSender emailSender;

    private Event createTestEvent() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        return new Event("Test Event", "Description", start, start.plusHours(2), 100, "img.url", "event.url", "loc", false);
    }

    @Test
    @DisplayName("Should send confirmation email successfully")
    void sendRegistrationConfirmation_shouldCallEmailSender() {
        // Arrange
        Event event = createTestEvent();
        String email = "test@example.com";
        doNothing().when(emailSender).sendEmail(any(EmailRequestDTO.class));

        // Act
        eventNotificationAdapter.sendRegistrationConfirmation(event, email);

        // Assert
        verify(emailSender, times(1)).sendEmail(any(EmailRequestDTO.class));
    }

    @Test
    @DisplayName("Should handle exceptions when sending confirmation email")
    void sendRegistrationConfirmation_whenEmailSenderFails_shouldHandleException() {
        // Arrange
        Event event = createTestEvent();
        String email = "test@example.com";
        doThrow(new RuntimeException("Email service down")).when(emailSender).sendEmail(any(EmailRequestDTO.class));

        // Act & Assert
        // O teste passa se nenhuma exceção for propagada para fora do método
        eventNotificationAdapter.sendRegistrationConfirmation(event, email);
        verify(emailSender, times(1)).sendEmail(any(EmailRequestDTO.class));
    }

    @Test
    @DisplayName("Should notify all participants of cancellation")
    void notifyParticipantsOfCancellation_shouldSendEmailToAllSubscribers() {
        // Arrange
        Event event = createTestEvent();
        Subscription sub1 = new Subscription(event, "user1@test.com");
        Subscription sub2 = new Subscription(event, "user2@test.com");
        List<Subscription> subscriptions = List.of(sub1, sub2);

        when(subscriptionRepository.findByEvent(event)).thenReturn(subscriptions);
        doNothing().when(emailSender).sendEmail(any(EmailRequestDTO.class));

        // Act
        eventNotificationAdapter.notifyParticipantsOfCancellation(event);

        // Assert
        verify(subscriptionRepository, times(1)).findByEvent(event);
        verify(emailSender, times(2)).sendEmail(any(EmailRequestDTO.class));
    }

    @Test
    @DisplayName("Should continue notifying other participants if one email fails")
    void notifyParticipantsOfCancellation_whenOneEmailFails_shouldContinueNotifying() {
        // Arrange
        Event event = createTestEvent();
        Subscription sub1 = new Subscription(event, "user1@test.com");
        Subscription sub2 = new Subscription(event, "user2@test.com");
        List<Subscription> subscriptions = List.of(sub1, sub2);

        when(subscriptionRepository.findByEvent(event)).thenReturn(subscriptions);
        // Simula falha para o primeiro e-mail e sucesso para o segundo
        doThrow(new RuntimeException("Failed to send")).doNothing().when(emailSender).sendEmail(any(EmailRequestDTO.class));

        // Act
        eventNotificationAdapter.notifyParticipantsOfCancellation(event);

        // Assert
        verify(subscriptionRepository, times(1)).findByEvent(event);
        // Verifica que o método de envio foi chamado para ambos os participantes, mesmo com a falha
        verify(emailSender, times(2)).sendEmail(any(EmailRequestDTO.class));
    }
}
