import api from "@/app/modules/api.service";
import { CreateEventRequest } from "@/app/modules/analytics/event/types/event.type";

/** Sends a single analytics event immediately to the backend */
export const sendEvent = async (event: CreateEventRequest): Promise<void> => {
  try {
    await api.post("/events", event);
  } catch (error) {
    console.error("Error sending analytics event:", error);
  }
};
