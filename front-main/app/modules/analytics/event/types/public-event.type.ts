export type PublicEventType = "PAGE_VIEW" | "CLICK" | "SEARCH";

export interface PublicEventCreateRequest {
  sessionId: string;
  eventType: PublicEventType;
  metadata?: Record<string, unknown>;
}
