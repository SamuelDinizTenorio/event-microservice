package com.Samuel.event_microservice.infrastructure.repositories;

import com.Samuel.event_microservice.core.models.Event;
import com.Samuel.event_microservice.core.models.EventStatus;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
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

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
        // Desabilita o ddl-auto para ter controle total
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaEventRepository jpaEventRepository;

    @BeforeEach
    void setup() {
        // Executa o Flyway manualmente antes de cada teste
        Flyway.configure().dataSource(dataSource).load().migrate();
    }

    @Test
    @DisplayName("findUpcomingEvents should return only active and future events")
    void findUpcomingEvents_shouldReturnOnlyActiveAndFutureEvents() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Cenário 1: Evento futuro e ativo (DEVE ser encontrado)
        Event upcomingActiveEvent = Event.builder()
                .title("Evento Futuro Ativo")
                .startDateTime(now.plusDays(1))
                .endDateTime(now.plusDays(2))
                .status(EventStatus.ACTIVE)
                .build();
        entityManager.persist(upcomingActiveEvent);

        // Cenário 2: Evento passado e ativo (NÃO deve ser encontrado)
        Event pastEvent = Event.builder()
                .title("Evento Passado")
                .startDateTime(now.minusDays(2))
                .endDateTime(now.minusDays(1))
                .status(EventStatus.ACTIVE)
                .build();
        entityManager.persist(pastEvent);

        // Cenário 3: Evento futuro, mas cancelado (NÃO deve ser encontrado)
        Event upcomingCancelledEvent = Event.builder()
                .title("Evento Futuro Cancelado")
                .startDateTime(now.plusDays(3))
                .endDateTime(now.plusDays(4))
                .status(EventStatus.CANCELLED)
                .build();
        entityManager.persist(upcomingCancelledEvent);

        entityManager.flush();

        // Act
        Page<Event> resultPage = jpaEventRepository.findUpcomingEvents(now, PageRequest.of(0, 10));

        // Assert
        // 1. Verifica os metadados da página
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getTotalPages()).isEqualTo(1);

        // 2. Verifica o conteúdo da página
        assertThat(resultPage.getContent()) // Foco na lista de eventos
                .hasSize(1) // Verifica se a lista tem exatamente um item.
                .first()
                .satisfies(event -> {
                    assertThat(event.getTitle()).isEqualTo(upcomingActiveEvent.getTitle());
                    assertThat(event.getStartDateTime()).isEqualTo(upcomingActiveEvent.getStartDateTime());
                    assertThat(event.getEndDateTime()).isEqualTo(upcomingActiveEvent.getEndDateTime());
                    assertThat(event.getStatus()).isEqualTo(upcomingActiveEvent.getStatus());
                });
    }

    @Test
    @DisplayName("findAll should return only active events")
    void findAll_shouldReturnOnlyActiveEvents() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Cenário 1: Evento ativo (DEVE ser encontrado)
        Event activeEvent = Event.builder()
                .title("Evento Ativo")
                .startDateTime(now.plusDays(1))
                .endDateTime(now.plusDays(2))
                .status(EventStatus.ACTIVE)
                .build();
        entityManager.persist(activeEvent);

        // Cenário 2: Evento cancelado (NÃO deve ser encontrado)
        Event cancelledEvent = Event.builder()
                .title("Evento Cancelado")
                .startDateTime(now.plusDays(3))
                .endDateTime(now.plusDays(4))
                .status(EventStatus.CANCELLED)
                .build();
        entityManager.persist(cancelledEvent);

        entityManager.flush();

        // Act
        Page<Event> resultPage = jpaEventRepository.findAll(PageRequest.of(0, 10));

        // Assert
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getTotalPages()).isEqualTo(1);

        assertThat(resultPage.getContent())
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getTitle()).isEqualTo(activeEvent.getTitle());
                    assertThat(event.getStartDateTime()).isEqualTo(activeEvent.getStartDateTime());
                    assertThat(event.getEndDateTime()).isEqualTo(activeEvent.getEndDateTime());
                    assertThat(event.getStatus()).isEqualTo(activeEvent.getStatus());
                });
    }

    @Test
    @DisplayName("findActiveEventsFinishedBefore should return only active events that have ended")
    void findActiveEventsFinishedBefore_shouldReturnOnlyActiveAndFinishedEvents() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Cenário 1: Evento ativo que já terminou (DEVE ser encontrado)
        Event activeAndFinished = Event.builder()
                .title("Active and Finished")
                .startDateTime(now.minusDays(2))
                .endDateTime(now.minusDays(1))
                .status(EventStatus.ACTIVE)
                .build();
        entityManager.persist(activeAndFinished);

        // Cenário 2: Evento ativo que ainda não terminou (NÃO deve ser encontrado)
        Event activeAndUpcoming = Event.builder()
                .title("Active and Upcoming")
                .startDateTime(now.plusDays(1))
                .endDateTime(now.plusDays(2))
                .status(EventStatus.ACTIVE)
                .build();
        entityManager.persist(activeAndUpcoming);

        // Cenário 3: Evento cancelado que já terminou (NÃO deve ser encontrado)
        Event cancelledAndFinished = Event.builder()
                .title("Cancelled and Finished")
                .startDateTime(now.minusDays(3))
                .endDateTime(now.minusDays(2))
                .status(EventStatus.CANCELLED)
                .build();
        entityManager.persist(cancelledAndFinished);

        entityManager.flush();

        // Act
        List<Event> results = jpaEventRepository.findActiveEventsFinishedBefore(now);

        // Assert
        assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getTitle()).isEqualTo(activeAndFinished.getTitle());
                    assertThat(event.getStartDateTime()).isEqualTo(activeAndFinished.getStartDateTime());
                    assertThat(event.getEndDateTime()).isEqualTo(activeAndFinished.getEndDateTime());
                    assertThat(event.getStatus()).isEqualTo(activeAndFinished.getStatus());
                });
    }
}
