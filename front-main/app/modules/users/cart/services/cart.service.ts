import api from "@/app/modules/api.service";
import { getUserHeaders } from "@/app/modules/users/session/services/session.service";
import type {
  AddToCartRequest,
  Cart,
  MergeCartRequest,
  UpdateCartItemRequest,
} from "../types/cart.type";

export const getCart = async (): Promise<Cart> => {
  const response = await api.get<Cart>("/cart", { headers: getUserHeaders() });

  return response.data;
};

export const addCartItem = async (request: AddToCartRequest): Promise<Cart> => {
  const response = await api.post<Cart>("/cart/items", request, { headers: getUserHeaders() });

  return response.data;
};

export const updateCartItem = async (
  productId: string,
  request: UpdateCartItemRequest
): Promise<Cart> => {
  const response = await api.patch<Cart>(
    `/cart/items/${encodeURIComponent(productId)}`,
    request,
    { headers: getUserHeaders() }
  );

  return response.data;
};

export const removeCartItem = async (productId: string): Promise<Cart> => {
  const response = await api.delete<Cart>(
    `/cart/items/${encodeURIComponent(productId)}`,
    { headers: getUserHeaders() }
  );

  return response.data;
};

export const clearCart = async (): Promise<Cart> => {
  const response = await api.delete<Cart>("/cart", { headers: getUserHeaders() });

  return response.data;
};

export const mergeCart = async (request: MergeCartRequest): Promise<Cart> => {
  const response = await api.post<Cart>("/cart/merge", request, { headers: getUserHeaders() });

  return response.data;
};
