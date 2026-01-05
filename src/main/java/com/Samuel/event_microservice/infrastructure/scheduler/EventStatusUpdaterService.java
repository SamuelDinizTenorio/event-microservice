package com.Samuel.event_microservice.infrastructure.scheduler;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStatusUpdaterService {

    private final EventRepositoryPort eventRepository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedRate = 3600000)
    public void updateFinishedEvents() {
        log.info("Running scheduled job to update finished events...");
        LocalDateTime now = LocalDateTime.now();

        // A busca de eventos pode ser feita fora de uma transação
        List<Event> finishedEvents = eventRepository.findActiveEventsFinishedBefore(now);

        if (finishedEvents.isEmpty()) {
            log.info("No events to update.");
            return;
        }

        log.info("Found {} events to mark as FINISHED.", finishedEvents.size());
        for (Event event : finishedEvents) {
            // Executa a lógica para cada evento em sua própria transação programática
            transactionTemplate.execute(status -> {
                try {
                    // Recarrega a entidade dentro da nova transação para evitar problemas de detached entity
                    Event managedEvent = eventRepository.findById(event.getId()).orElse(null);
                    if (managedEvent == null) return null;

                    managedEvent.finish();
                    eventRepository.save(managedEvent);
                    log.info("Event with ID {} marked as FINISHED.", managedEvent.getId());
                } catch (Exception e) {
                    log.error("Failed to update status for event {}: {}", event.getId(), e.getMessage());
                    // Não relança a exceção, permitindo que o loop for continue
                }
                return null; // Retorno necessário para o execute
            });
        }

        log.info("Finished updating event statuses.");
    }
}
