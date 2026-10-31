import type { Cart, CartItem, CartProductSnapshot } from "../types/cart.type";

const STORAGE_KEY = "netcomp_guest_cart";
export const MAX_QUANTITY_PER_PRODUCT = 99;

const isCartItem = (value: unknown): value is CartItem => {
  if (!value || typeof value !== "object") return false;
  const item = value as Partial<CartItem>;
  return typeof item.productId === "string"
    && typeof item.productName === "string"
    && typeof item.unitPrice === "number"
    && typeof item.quantity === "number"
    && item.quantity > 0;
};

export const buildCart = (items: CartItem[]): Cart => {
  const normalizedItems = items.map((item) => ({
    ...item,
    subtotal: Math.round(item.unitPrice * item.quantity * 100) / 100,
    available: item.quantity <= item.availableStock,
  }));

  return {
    userId: null,
    items: normalizedItems,
    itemCount: normalizedItems.reduce((sum, item) => sum + item.quantity, 0),
    total: Math.round(normalizedItems.reduce((sum, item) => sum + item.subtotal, 0) * 100) / 100,
    checkoutAvailable: normalizedItems.length > 0 && normalizedItems.every((item) => item.available),
  };
};

export const readGuestItems = (): CartItem[] => {
  if (typeof window === "undefined") return [];
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    const parsed: unknown = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed.filter(isCartItem) : [];
  } catch {
    return [];
  }
};

export const writeGuestItems = (items: CartItem[]): void => {
  if (typeof window === "undefined") return;
  try {
    if (items.length === 0) {
      window.localStorage.removeItem(STORAGE_KEY);
      return;
    }
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  } catch {
    return;
  }
};

export const addGuestItem = (
  items: CartItem[],
  product: CartProductSnapshot,
  quantity: number
): CartItem[] => {
  const existing = items.find((item) => item.productId === product.productId);
  const requestedQuantity = (existing?.quantity ?? 0) + quantity;
  const limit = Math.min(product.availableStock, MAX_QUANTITY_PER_PRODUCT);

  if (product.availableStock <= 0) {
    throw new Error(`El producto ${product.productName} está agotado.`);
  }
  if (requestedQuantity > limit) {
    throw new Error(
      `Solo hay ${product.availableStock} unidades disponibles de ${product.productName}.`
    );
  }

  if (existing) {
    return items.map((item) => item.productId === product.productId
      ? { ...item, ...product, quantity: requestedQuantity }
      : item);
  }

  return [
    ...items,
    {
      ...product,
      id: product.productId,
      quantity: requestedQuantity,
      subtotal: product.unitPrice * requestedQuantity,
      available: true,
      addedAt: new Date().toISOString(),
    },
  ];
};

export const updateGuestItem = (
  items: CartItem[],
  productId: string,
  quantity: number
): CartItem[] => items.map((item) => {
  if (item.productId !== productId) return item;
  if (quantity > Math.min(item.availableStock, MAX_QUANTITY_PER_PRODUCT)) {
    throw new Error(
      `Solo hay ${item.availableStock} unidades disponibles de ${item.productName}.`
    );
  }
  return { ...item, quantity };
});

export const removeGuestItem = (items: CartItem[], productId: string): CartItem[] =>
  items.filter((item) => item.productId !== productId);
