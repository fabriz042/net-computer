export interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productSlug: string;
  categorySlug: string | null;
  brandName: string | null;
  imageUrl: string | null;
  unitPrice: number;
  quantity: number;
  availableStock: number;
  subtotal: number;
  available: boolean;
  addedAt: string | null;
}

export interface Cart {
  userId: string | null;
  items: CartItem[];
  itemCount: number;
  total: number;
  checkoutAvailable: boolean;
}

export interface AddToCartRequest {
  productId: string;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export interface MergeCartRequest {
  items: AddToCartRequest[];
}

export interface CartProductSnapshot {
  productId: string;
  productName: string;
  productSlug: string;
  categorySlug: string | null;
  brandName: string | null;
  imageUrl: string | null;
  unitPrice: number;
  availableStock: number;
}
