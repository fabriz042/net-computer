import api from "@/app/modules/api.service";
import type { PublicEventCreateRequest } from "@/app/modules/analytics/event/types/public-event.type";

/** Sends a single public analytics event immediately to the backend */
export const sendPublicEvent = async (event: PublicEventCreateRequest): Promise<void> => {
  try {
    await api.post("/events", event);
  } catch (error) {
    console.error("Error sending analytics event:", error);
  }
};
