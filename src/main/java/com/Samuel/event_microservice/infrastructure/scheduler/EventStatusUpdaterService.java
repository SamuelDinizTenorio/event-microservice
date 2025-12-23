package com.Samuel.event_microservice.infrastructure.scheduler;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.ports.EventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço agendado para gerenciar o ciclo de vida dos status dos eventos.
 */
@Service
@RequiredArgsConstructor
public class EventStatusUpdaterService {

    private static final Logger logger = LoggerFactory.getLogger(EventStatusUpdaterService.class);

    private final EventRepositoryPort eventRepository;

    /**
     * Tarefa agendada que roda a cada hora para atualizar o status de eventos.
     * <p>
     * Este método busca por eventos que estão com status 'ACTIVE' mas cuja data de término
     * já passou, e atualiza seu status para 'FINISHED'.
     */
    @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 hora
    @Transactional
    public void updateFinishedEvents() {
        logger.info("Running scheduled job to update finished events...");
        LocalDateTime now = LocalDateTime.now();
        
        List<Event> finishedEvents = eventRepository.findActiveEventsFinishedBefore(now);

        if (finishedEvents.isEmpty()) {
            logger.info("No events to update.");
            return;
        }

        logger.info("Found {} events to mark as FINISHED.", finishedEvents.size());
        for (Event event : finishedEvents) {
            try {
                event.finish();
                eventRepository.save(event);
                logger.info("Event with ID {} marked as FINISHED.", event.getId());
            } catch (Exception e) {
                logger.error("Failed to update status for event {}: {}", event.getId(), e.getMessage());
            }
        }
        
        logger.info("Finished updating event statuses.");
    }
}
