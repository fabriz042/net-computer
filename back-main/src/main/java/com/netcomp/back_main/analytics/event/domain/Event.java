package com.netcomp.back_main.analytics.event.domain;

import com.netcomp.back_main.users.user.domain.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_session_type_created_at", columnList = "session_id, event_type, created_at")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // constructors
    public Event() {
    }

    public Event(String sessionId, EventType eventType, User user, Map<String, Object> metadata) {
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.user = user;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public Event(UUID id, Instant createdAt, String sessionId, EventType eventType, User user, Map<String, Object> metadata) {
        this.id = id;
        this.createdAt = createdAt;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.user = user;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // getters
    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public User getUser() {
        return user;
    }

    // setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
