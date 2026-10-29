import { sendPublicEvent } from "@/app/modules/analytics/event/services/public-event.service";
import type {
  PublicEventCreateRequest,
  PublicEventType,
} from "@/app/modules/analytics/event/types/public-event.type";

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

/** Dispatches a public analytics event immediately to the backend */
export const trackPublicEvent = (
  eventType: PublicEventType,
  metadata?: Record<string, unknown>
): void => {
  if (typeof window === "undefined") return;

  const event: PublicEventCreateRequest = {
    sessionId: getSessionId(),
    eventType,
    metadata: {
      page: window.location.pathname,
      ...metadata,
    },
  };

  sendPublicEvent(event);
};
