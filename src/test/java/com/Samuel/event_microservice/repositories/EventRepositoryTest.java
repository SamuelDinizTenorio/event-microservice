package com.Samuel.event_microservice.repositories;

import com.Samuel.event_microservice.domain.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("Should return only upcoming events when findUpcomingEvents is called")
    void findUpcomingEvents_shouldReturnOnlyFutureEvents() {
        // Arrange
        Event pastEvent = Event.builder()
                .title("Evento Passado")
                .date(LocalDateTime.now().minusDays(1))
                .maxParticipants(100)
                .registeredParticipants(0)
                .isRemote(false)
                .build();
        entityManager.persist(pastEvent);

        Event upcomingEvent = Event.builder()
                .title("Evento Futuro")
                .date(LocalDateTime.now().plusDays(1))
                .maxParticipants(100)
                .registeredParticipants(0)
                .isRemote(true)
                .build();
        entityManager.persist(upcomingEvent);

        entityManager.flush();

        // Act
        Page<Event> resultPage = eventRepository.findUpcomingEvents(LocalDateTime.now(), PageRequest.of(0, 10));

        // Assert
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Evento Futuro", resultPage.getContent().get(0).getTitle());
        assertTrue(resultPage.getContent().get(0).getDate().isAfter(LocalDateTime.now()));
    }
}
