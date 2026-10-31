import Cookies from "js-cookie";

const USER_ID_COOKIE = "userId";
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export const getCurrentUserId = (): string | null => {
  if (typeof window === "undefined") return null;
  const userId = Cookies.get(USER_ID_COOKIE);
  return userId && UUID_PATTERN.test(userId) ? userId : null;
};

export const getUserHeaders = (): Record<string, string> => {
  const userId = getCurrentUserId();
  return userId ? { "X-User-Id": userId } : {};
};
