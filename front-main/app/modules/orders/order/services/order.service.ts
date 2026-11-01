import api from "@/app/modules/api.service";
import { getUserHeaders } from "@/app/modules/users/session/services/session.service";
import type {
  CreateGuestOrderRequest,
  CreateOrderRequest,
  Order,
  OrderListParams,
  OrderListResponse,
} from "../types/order.type";

const GUEST_ORDER_STORAGE_KEY = "netcomp_guest_order";

export const createOrder = async (request: CreateOrderRequest): Promise<Order> => {
  const response = await api.post<Order>("/orders", request, { headers: getUserHeaders() });

  return response.data;
};

export const createGuestOrder = async (request: CreateGuestOrderRequest): Promise<Order> => {
  const response = await api.post<Order>("/orders/guest", request);

  return response.data;
};

export const saveGuestOrder = (order: Order): void => {
  if (typeof window === "undefined") return;
  try {
    window.sessionStorage.setItem(GUEST_ORDER_STORAGE_KEY, JSON.stringify(order));
  } catch {
    return;
  }
};

export const readGuestOrder = (orderId: string): Order | null => {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.sessionStorage.getItem(GUEST_ORDER_STORAGE_KEY);
    const order = raw ? (JSON.parse(raw) as Order) : null;
    return order?.id === orderId ? order : null;
  } catch {
    return null;
  }
};

export const listOrders = async (params: OrderListParams = {}): Promise<OrderListResponse> => {
  const response = await api.get<OrderListResponse>("/orders", {
    params,
    headers: getUserHeaders(),
  });

  return response.data;
};

export const getOrder = async (orderId: string): Promise<Order> => {
  const response = await api.get<Order>(`/orders/${encodeURIComponent(orderId)}`, {
    headers: getUserHeaders(),
  });

  return response.data;
};

export const cancelOrder = async (orderId: string): Promise<Order> => {
  const response = await api.post<Order>(
    `/orders/${encodeURIComponent(orderId)}/cancel`,
    null,
    { headers: getUserHeaders() }
  );

  return response.data;
};
