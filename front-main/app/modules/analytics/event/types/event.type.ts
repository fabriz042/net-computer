export type EventType = "PAGE_VIEW" | "CLICK" | "SEARCH";

export interface CreateEventRequest {
  sessionId: string;
  eventType: EventType;
  metadata?: Record<string, unknown>;
}
