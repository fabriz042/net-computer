package com.netcomp.back_main.analytics.event.repository;

import com.netcomp.back_main.analytics.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
