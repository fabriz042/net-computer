import api from "@/app/modules/api.service";
import { getUserHeaders } from "@/app/modules/users/session/services/session.service";
import type {
  CreateOrderRequest,
  Order,
  OrderListParams,
  OrderListResponse,
} from "../types/order.type";

export const createOrder = async (request: CreateOrderRequest): Promise<Order> => {
  const response = await api.post<Order>("/orders", request, { headers: getUserHeaders() });

  return response.data;
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
