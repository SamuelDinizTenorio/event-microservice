package com.Samuel.event_microservice.infrastructure.repositories;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.EventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Testcontainers // Habilita o suporte a Testcontainers nesta classe de teste
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Desabilita a substituição pelo H2
class JpaEventRepositoryTest {

    // Define um container do PostgreSQL que será iniciado antes dos testes
    @Container
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    // Configura dinamicamente as propriedades do Spring para se conectar ao container
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.flyway.enabled", () -> "true"); // Garante que o Flyway rode
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaEventRepository jpaEventRepository;

    @Test
    @DisplayName("findUpcomingEvents should return only active and future events")
    void findUpcomingEvents_shouldReturnOnlyActiveAndFutureEvents() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Cenário 1: Evento futuro e ativo (DEVE ser encontrado)
        Event upcomingActiveEvent = Event.builder().title("Evento Futuro Ativo").startDateTime(now.plusDays(1)).endDateTime(now.plusDays(2)).status(EventStatus.ACTIVE).build();
        entityManager.persist(upcomingActiveEvent);

        // Cenário 2: Evento passado e ativo (NÃO deve ser encontrado)
        Event pastEvent = Event.builder().title("Evento Passado").startDateTime(now.minusDays(2)).endDateTime(now.minusDays(1)).status(EventStatus.ACTIVE).build();
        entityManager.persist(pastEvent);

        // Cenário 3: Evento futuro mas cancelado (NÃO deve ser encontrado)
        Event upcomingCancelledEvent = Event.builder().title("Evento Futuro Cancelado").startDateTime(now.plusDays(3)).endDateTime(now.plusDays(4)).status(EventStatus.CANCELLED).build();
        entityManager.persist(upcomingCancelledEvent);

        entityManager.flush();

        // Act
        Page<Event> resultPage = jpaEventRepository.findUpcomingEvents(now, PageRequest.of(0, 10));

        // Assert
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Evento Futuro Ativo", resultPage.getContent().getFirst().getTitle());
    }

    @Test
    @DisplayName("findAll should return only active events")
    void findAll_shouldReturnOnlyActiveEvents() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Cenário 1: Evento ativo (DEVE ser encontrado)
        Event activeEvent = Event.builder().title("Evento Ativo").startDateTime(now.plusDays(1)).endDateTime(now.plusDays(2)).status(EventStatus.ACTIVE).build();
        entityManager.persist(activeEvent);

        // Cenário 2: Evento cancelado (NÃO deve ser encontrado)
        Event cancelledEvent = Event.builder().title("Evento Cancelado").startDateTime(now.plusDays(3)).endDateTime(now.plusDays(4)).status(EventStatus.CANCELLED).build();
        entityManager.persist(cancelledEvent);

        entityManager.flush();

        // Act
        Page<Event> resultPage = jpaEventRepository.findAll(PageRequest.of(0, 10));

        // Assert
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Evento Ativo", resultPage.getContent().getFirst().getTitle());
    }
}
