const STORE_TIME_ZONE = "America/Lima";

export const formatPrice = (price: number): string =>
  price.toLocaleString("es-PE", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

export const formatDateTime = (value: string): string =>
  new Date(value).toLocaleString("es-PE", {
    timeZone: STORE_TIME_ZONE,
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

export const formatShortDate = (value: string): string => {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(Date.UTC(year, month - 1, day)).toLocaleDateString("es-PE", {
    timeZone: "UTC",
    day: "2-digit",
    month: "short",
  });
};

export const formatDate = (value: string): string => {
  const [year, month, day] = value.split("-");
  return `${day}/${month}/${year}`;
};

export const toStoreDateInput = (date: Date): string =>
  new Intl.DateTimeFormat("en-CA", {
    timeZone: STORE_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
