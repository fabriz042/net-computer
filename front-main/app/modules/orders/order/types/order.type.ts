export type OrderStatusCode =
  | "PENDING"
  | "PAID"
  | "PROCESSING"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED";

export interface OrderItem {
  id: string;
  productId: string | null;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: string;
  code: string;
  status: OrderStatusCode;
  statusName: string;
  nextStatuses: OrderStatusCode[];
  total: number;
  itemCount: number;
  shippingAddress: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  items: OrderItem[];
  createdAt: string;
}

export interface OrderListResponse {
  meta: {
    total_items: number;
    current_page: number;
    per_page: number;
    total_pages: number;
    has_next: boolean;
    has_previous: boolean;
  };
  data: Order[];
}

export interface CreateOrderRequest {
  shippingAddress: string;
}

export interface CreateGuestOrderRequest {
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  items: { productId: string; quantity: number }[];
}

export interface OrderListParams {
  page?: number;
  per_page?: number;
}
