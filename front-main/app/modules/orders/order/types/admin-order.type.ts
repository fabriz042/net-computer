import type { OrderStatusCode } from "./order.type";

export type AdminOrderSortOrder = "asc" | "desc";

export interface AdminOrderFilters {
  search?: string;
  status?: OrderStatusCode;
  from?: string;
  to?: string;
}

export interface AdminOrderListParams extends AdminOrderFilters {
  order?: AdminOrderSortOrder;
  page?: number;
  per_page?: number;
}

export interface OrderStatusOption {
  code: OrderStatusCode;
  name: string;
}

export interface UpdateOrderStatusRequest {
  status: OrderStatusCode;
}

export interface SalesSummaryParams {
  from?: string;
  to?: string;
}

export interface SalesStatusTotal {
  code: OrderStatusCode;
  name: string;
  orderCount: number;
  total: number;
}

export interface DailySale {
  date: string;
  orderCount: number;
  revenue: number;
}

export interface TopProduct {
  productId: string;
  productName: string;
  unitsSold: number;
  revenue: number;
}

export interface SalesSummary {
  from: string;
  to: string;
  totalRevenue: number;
  completedOrders: number;
  averageTicket: number;
  unitsSold: number;
  pendingOrders: number;
  pendingAmount: number;
  cancelledOrders: number;
  statusBreakdown: SalesStatusTotal[];
  dailySales: DailySale[];
  topProducts: TopProduct[];
}
