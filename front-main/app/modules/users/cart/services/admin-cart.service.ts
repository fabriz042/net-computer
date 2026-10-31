import api from "@/app/modules/api.service";
import type { Cart } from "../types/cart.type";
import type {
  AdminCartListParams,
  AdminCartListResponse,
} from "../types/admin-cart.type";

export const listAdminCarts = async (
  params: AdminCartListParams = {}
): Promise<AdminCartListResponse> => {
  const response = await api.get<AdminCartListResponse>("/admin/carts", { params });

  return response.data;
};

export const getAdminCart = async (userId: string): Promise<Cart> => {
  const response = await api.get<Cart>(`/admin/carts/${encodeURIComponent(userId)}`);

  return response.data;
};
