package com.Samuel.event_microservice.infrastructure.adapters;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.Subscription;
import com.Samuel.event_microservice.core.ports.EmailSender;
import com.Samuel.event_microservice.core.ports.EventNotificationPort;
import com.Samuel.event_microservice.core.ports.SubscriptionRepositoryPort;
import com.Samuel.event_microservice.infrastructure.dto.EmailRequestDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador que implementa a porta de notificação de eventos usando um serviço de e-mail.
 */
@Component 
@RequiredArgsConstructor
public class EventNotificationAdapter implements EventNotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(EventNotificationAdapter.class);

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final EmailSender emailSender;

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyParticipantsOfCancellation(Event event) {
        try {
            List<Subscription> subscriptions = subscriptionRepository.findByEvent(event);
            logger.info("Found {} participants to notify for cancellation of event {}", subscriptions.size(), event.getId());

            for (Subscription sub : subscriptions) {
                try {
                    EmailRequestDTO emailRequest = new EmailRequestDTO(
                            sub.getParticipantEmail(),
                            "Evento Cancelado: " + event.getTitle(),
                            "Lamentamos informar que o evento '" + event.getTitle() + "', que ocorreria em " + event.getStartDateTime() + ", foi cancelado."
                    );
                    emailSender.sendEmail(emailRequest);
                    logger.info("Cancellation email sent to {}.", sub.getParticipantEmail());
                } catch (Exception e) {
                    logger.error("Failed to send cancellation email to {} for event {}: {}", sub.getParticipantEmail(), event.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("A critical error occurred while trying to notify participants for event {}: {}", event.getId(), e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendRegistrationConfirmation(Event event, String participantEmail) {
        try {
            logger.info("Sending confirmation email to {}.", participantEmail);
            EmailRequestDTO emailRequest = new EmailRequestDTO(
                    participantEmail,
                    "Inscrição Confirmada: " + event.getTitle(),
                    "Sua inscrição no evento '" + event.getTitle() + "' foi confirmada com sucesso!"
            );
            emailSender.sendEmail(emailRequest);
            logger.info("Confirmation email sent successfully to {}.", participantEmail);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {} for event {}: {}", participantEmail, event.getId(), e.getMessage());
        }
    }
}
