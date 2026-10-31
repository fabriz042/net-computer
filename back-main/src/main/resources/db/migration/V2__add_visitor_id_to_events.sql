ALTER TABLE events
    ADD COLUMN visitor_id UUID;

CREATE INDEX idx_events_visitor_type_created_at
    ON events (visitor_id, event_type, created_at);
