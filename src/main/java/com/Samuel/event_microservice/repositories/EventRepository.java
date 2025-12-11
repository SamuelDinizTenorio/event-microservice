package com.Samuel.event_microservice.repositories;

import com.Samuel.event_microservice.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM event e WHERE e.date > :currentDate")
    Page<Event> findUpcomingEvents(@Param("currentDate") LocalDate currentDate, Pageable pageable);
}
