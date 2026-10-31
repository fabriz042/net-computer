import api from "@/app/modules/api.service";
import type { Order, OrderListResponse } from "../types/order.type";
import type {
  AdminOrderFilters,
  AdminOrderListParams,
  OrderStatusOption,
  SalesSummary,
  SalesSummaryParams,
  UpdateOrderStatusRequest,
} from "../types/admin-order.type";

const REPORT_TIMEOUT_MS = 60000;

export const listAdminOrders = async (
  params: AdminOrderListParams = {}
): Promise<OrderListResponse> => {
  const response = await api.get<OrderListResponse>("/admin/orders", { params });

  return response.data;
};

export const getAdminOrder = async (orderId: string): Promise<Order> => {
  const response = await api.get<Order>(`/admin/orders/${encodeURIComponent(orderId)}`);

  return response.data;
};

export const listAdminOrderStatuses = async (): Promise<OrderStatusOption[]> => {
  const response = await api.get<OrderStatusOption[]>("/admin/orders/statuses");

  return response.data;
};

export const updateAdminOrderStatus = async (
  orderId: string,
  request: UpdateOrderStatusRequest
): Promise<Order> => {
  const response = await api.patch<Order>(
    `/admin/orders/${encodeURIComponent(orderId)}/status`,
    request
  );

  return response.data;
};

export const getAdminSalesSummary = async (
  params: SalesSummaryParams = {}
): Promise<SalesSummary> => {
  const response = await api.get<SalesSummary>("/admin/orders/summary", { params });

  return response.data;
};

export const exportAdminOrders = async (filters: AdminOrderFilters = {}): Promise<Blob> => {
  const response = await api.get<Blob>("/admin/orders/export", {
    params: filters,
    responseType: "blob",
    timeout: REPORT_TIMEOUT_MS,
  });

  return response.data;
};
