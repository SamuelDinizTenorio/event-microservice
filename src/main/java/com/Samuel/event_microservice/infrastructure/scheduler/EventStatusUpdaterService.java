package com.Samuel.event_microservice.infrastructure.scheduler;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate; // Importar

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventStatusUpdaterService {

    private static final Logger logger = LoggerFactory.getLogger(EventStatusUpdaterService.class);

    private final EventRepositoryPort eventRepository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedRate = 3600000)
    public void updateFinishedEvents() {
        logger.info("Running scheduled job to update finished events...");
        LocalDateTime now = LocalDateTime.now();

        // A busca de eventos pode ser feita fora de uma transação
        List<Event> finishedEvents = eventRepository.findActiveEventsFinishedBefore(now);

        if (finishedEvents.isEmpty()) {
            logger.info("No events to update.");
            return;
        }

        logger.info("Found {} events to mark as FINISHED.", finishedEvents.size());
        for (Event event : finishedEvents) {
            // Executa a lógica para cada evento em sua própria transação programática
            transactionTemplate.execute(status -> {
                try {
                    // Recarrega a entidade dentro da nova transação para evitar problemas de detached entity
                    Event managedEvent = eventRepository.findById(event.getId()).orElse(null);
                    if (managedEvent == null) return null;

                    managedEvent.finish();
                    eventRepository.save(managedEvent);
                    logger.info("Event with ID {} marked as FINISHED.", managedEvent.getId());
                } catch (Exception e) {
                    logger.error("Failed to update status for event {}: {}", event.getId(), e.getMessage());
                    // Não relança a exceção, permitindo que o loop for continue
                }
                return null; // Retorno necessário para o execute
            });
        }

        logger.info("Finished updating event statuses.");
    }
}