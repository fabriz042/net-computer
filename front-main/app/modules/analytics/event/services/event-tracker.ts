import { sendEvent } from "@/app/modules/analytics/event/services/event.service";
import { CreateEventRequest, EventType } from "@/app/modules/analytics/event/types/event.type";

const SESSION_STORAGE_KEY = "analytics_session_id";

/** Retrieves or generates the sessionId for the current browser session */
export const getSessionId = (): string => {
  if (typeof window === "undefined") return "server-session";
  try {
    let sessionId = sessionStorage.getItem(SESSION_STORAGE_KEY);
    if (!sessionId) {
      sessionId = crypto.randomUUID();
      sessionStorage.setItem(SESSION_STORAGE_KEY, sessionId);
    }
    return sessionId;
  } catch {
    return "fallback-session";
  }
};

/** Dispatches an analytics event immediately to the backend */
export const trackEvent = (
  eventType: EventType,
  metadata?: Record<string, unknown>
): void => {
  if (typeof window === "undefined") return;

  const event: CreateEventRequest = {
    sessionId: getSessionId(),
    eventType,
    metadata: {
      page: window.location.pathname,
      ...metadata,
    },
  };

  sendEvent(event);
};
