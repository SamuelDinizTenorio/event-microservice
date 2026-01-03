package com.Samuel.event_microservice.infrastructure.adapters;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.core.ports.EmailSender;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventNotificationAdapterTest {

    @InjectMocks
    private EventNotificationAdapter eventNotificationAdapter;

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;

    @Mock
    private EmailSender emailSender;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        this.testEvent = Event.builder()
                .title("Test Event")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Nested
    @DisplayName("Tests for sendRegistrationConfirmation")
    class SendRegistrationConfirmation {

        @Test
        @DisplayName("Should send confirmation email with correct details")
        void shouldSendConfirmationEmailWithCorrectDetails() {
            // Arrange
            String participantEmail = "test@example.com";
            ArgumentCaptor<EmailRequestDTO> emailCaptor = ArgumentCaptor.forClass(EmailRequestDTO.class);
            doNothing().when(emailSender).sendEmail(emailCaptor.capture());

            // Act
            eventNotificationAdapter.sendRegistrationConfirmation(testEvent, participantEmail);

            // Assert
            verify(emailSender, times(1)).sendEmail(any(EmailRequestDTO.class));

            EmailRequestDTO capturedEmail = emailCaptor.getValue();
            assertThat(capturedEmail.to()).isEqualTo(participantEmail);
            assertThat(capturedEmail.subject()).isEqualTo("Inscrição Confirmada: " + testEvent.getTitle());
            assertThat(capturedEmail.body()).contains("Sua inscrição no evento '" + testEvent.getTitle() + "' foi confirmada com sucesso!");
        }

        @Test
        @DisplayName("Should not throw exception when email sending fails")
        void shouldNotThrowException_whenEmailSendingFails() {
            // Arrange
            String participantEmail = "test@example.com";
            doThrow(new RuntimeException("Email service is down"))
                    .when(emailSender).sendEmail(any(EmailRequestDTO.class));

            // Act & Assert
            // O teste verifica que o adapter captura a exceção e não a propaga
            eventNotificationAdapter.sendRegistrationConfirmation(testEvent, participantEmail);
            verify(emailSender, times(1)).sendEmail(any(EmailRequestDTO.class));
        }
    }

    @Nested
    @DisplayName("Tests for notifyParticipantsOfCancellation")
    class NotifyParticipantsOfCancellation {

        @Test
        @DisplayName("Should send cancellation email to all subscribed participants")
        void shouldSendCancellationEmailToAllSubscribers() {
            // Arrange
            Subscription sub1 = Subscription.builder()
                    .participantEmail("user1@test.com")
                    .event(testEvent)
                    .build();
            Subscription sub2 = Subscription.builder()
                    .participantEmail("user2@test.com")
                    .event(testEvent)
                    .build();
            when(subscriptionRepository.findByEvent(testEvent))
                    .thenReturn(List.of(sub1, sub2));

            ArgumentCaptor<EmailRequestDTO> emailCaptor = ArgumentCaptor.forClass(EmailRequestDTO.class);
            doNothing().when(emailSender)
                    .sendEmail(emailCaptor.capture());

            // Act
            eventNotificationAdapter.notifyParticipantsOfCancellation(testEvent);

            // Assert
            verify(emailSender, times(2)).sendEmail(any(EmailRequestDTO.class));

            List<EmailRequestDTO> capturedEmails = emailCaptor.getAllValues();
            assertThat(capturedEmails).hasSize(2);

            assertThat(capturedEmails.get(0).to()).isEqualTo("user1@test.com");
            assertThat(capturedEmails.get(0).subject()).isEqualTo("Evento Cancelado: " + testEvent.getTitle());
            assertThat(capturedEmails.get(0).body()).contains("foi cancelado");

            assertThat(capturedEmails.get(1).to()).isEqualTo("user2@test.com");
        }

        @Test
        @DisplayName("Should continue notifying other participants if one email fails")
        void shouldContinueNotifying_whenOneEmailFails() {
            // Arrange
            Subscription sub1 = Subscription.builder()
                    .participantEmail("user1@test.com")
                    .event(testEvent)
                    .build();
            Subscription sub2 = Subscription.builder()
                    .participantEmail("user2@test.com")
                    .event(testEvent)
                    .build();
            when(subscriptionRepository.findByEvent(testEvent))
                    .thenReturn(List.of(sub1, sub2));

            // Simula falha para o primeiro e-mail e sucesso para o segundo
            doThrow(new RuntimeException("Failed to send"))
                    .doNothing().when(emailSender).sendEmail(any(EmailRequestDTO.class));

            // Act
            eventNotificationAdapter.notifyParticipantsOfCancellation(testEvent);

            // Assert
            // Verifica que o método de envio foi chamado para ambos os participantes, mesmo com a falha
            verify(emailSender, times(2)).sendEmail(any(EmailRequestDTO.class));
        }

        @Test
        @DisplayName("Should not fail if there are no participants to notify")
        void shouldNotFail_whenNoParticipants() {
            // Arrange
            when(subscriptionRepository.findByEvent(testEvent))
                    .thenReturn(List.of());

            // Act
            eventNotificationAdapter.notifyParticipantsOfCancellation(testEvent);

            // Assert
            // Verifica que o método de envio de e-mail nunca foi chamado
            verify(emailSender, never()).sendEmail(any(EmailRequestDTO.class));
        }
    }
}
