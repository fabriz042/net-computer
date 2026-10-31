const ANALYTICS_USER_ID_KEY = "analytics_user_id";

/** Retrieves or creates the persistent anonymous analytics user ID. */
export const getAnalyticsUserId = (): string | null => {
  if (typeof window === "undefined") return null;

  try {
    const storedUserId = window.localStorage.getItem(ANALYTICS_USER_ID_KEY);
    if (storedUserId) return storedUserId;

    const userId = window.crypto.randomUUID();
    window.localStorage.setItem(ANALYTICS_USER_ID_KEY, userId);
    return userId;
  } catch {
    return null;
  }
};
