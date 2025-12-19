package com.Samuel.event_microservice.infrastructure.repositories;

import com.Samuel.event_microservice.core.models.Event;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Page<Event> resultPage = jpaEventRepository.findUpcomingEvents(LocalDateTime.now(), PageRequest.of(0, 10));

        // Assert
        assertEquals(1, resultPage.getTotalElements());
        assertEquals("Evento Futuro", resultPage.getContent().get(0).getTitle());
        assertTrue(resultPage.getContent().get(0).getDate().isAfter(LocalDateTime.now()));
    }
}
