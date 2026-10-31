export type PublicEventType = "PAGE_VIEW" | "CLICK" | "SEARCH";

export interface PublicEventCreateRequest {
  sessionId: string;
  visitorId: string | null;
  eventType: PublicEventType;
  metadata?: Record<string, unknown>;
}
