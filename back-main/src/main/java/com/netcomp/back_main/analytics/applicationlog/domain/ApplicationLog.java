package com.netcomp.back_main.analytics.applicationlog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "application_logs", indexes = {
        @Index(name = "idx_application_logs_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_application_logs_level_occurred_at", columnList = "level, occurred_at")
})
public class ApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false, length = 10)
    private String level;

    @Column(name = "logger_name", nullable = false, columnDefinition = "text")
    private String loggerName;

    @Column(name = "thread_name", nullable = false, columnDefinition = "text")
    private String threadName;

    @Column(columnDefinition = "text")
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> mdc = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object throwable;

    @Column(name = "ingested_at", nullable = false, insertable = false, updatable = false)
    private Instant ingestedAt;

    protected ApplicationLog() {
    }

    public Long getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getMdc() {
        return mdc;
    }

    public Object getThrowable() {
        return throwable;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }
}
