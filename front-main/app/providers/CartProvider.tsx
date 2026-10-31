"use client";

import React, { createContext, useCallback, useEffect, useMemo, useState } from "react";
import { getApiErrorMessage, getApiErrorStatus } from "@/app/modules/api-error.service";
import { getCurrentUserId } from "@/app/modules/users/session/services/session.service";
import {
  addCartItem,
  clearCart as clearRemoteCart,
  getCart,
  mergeCart,
  removeCartItem,
  updateCartItem,
} from "@/app/modules/users/cart/services/cart.service";
import {
  addGuestItem,
  buildCart,
  readGuestItems,
  removeGuestItem,
  updateGuestItem,
  writeGuestItems,
} from "@/app/modules/users/cart/services/guest-cart.storage";
import type { Cart, CartProductSnapshot } from "@/app/modules/users/cart/types/cart.type";

export interface CartContextType {
  cart: Cart;
  isLoading: boolean;
  isAuthenticated: boolean;
  pendingProductId: string | null;
  addItem: (product: CartProductSnapshot, quantity: number) => Promise<void>;
  updateQuantity: (productId: string, quantity: number) => Promise<void>;
  removeItem: (productId: string) => Promise<void>;
  clear: () => Promise<void>;
  refresh: () => Promise<void>;
}

export const CartContext = createContext<CartContextType | null>(null);

const emptyCart: Cart = buildCart([]);

const resolveCart = async (): Promise<{ userId: string | null; cart: Cart }> => {
  const currentUserId = getCurrentUserId();
  if (!currentUserId) {
    return { userId: null, cart: buildCart(readGuestItems()) };
  }

  try {
    const guestItems = readGuestItems();
    if (guestItems.length === 0) {
      return { userId: currentUserId, cart: await getCart() };
    }
    const mergedCart = await mergeCart({
      items: guestItems.map((item) => ({ productId: item.productId, quantity: item.quantity })),
    });
    writeGuestItems([]);
    return { userId: currentUserId, cart: mergedCart };
  } catch (error) {
    const status = getApiErrorStatus(error);
    if (status === 401 || status === 403 || status === 404) {
      return { userId: null, cart: buildCart(readGuestItems()) };
    }
    return { userId: currentUserId, cart: emptyCart };
  }
};

interface CartProviderProps {
  children: React.ReactNode;
}

export function CartProvider({ children }: CartProviderProps) {
  const [cart, setCart] = useState<Cart>(emptyCart);
  const [isLoading, setIsLoading] = useState(true);
  const [userId, setUserId] = useState<string | null>(null);
  const [pendingProductId, setPendingProductId] = useState<string | null>(null);

  const applyResolvedCart = useCallback((resolved: { userId: string | null; cart: Cart }) => {
    setUserId(resolved.userId);
    setCart(resolved.cart);
    setIsLoading(false);
  }, []);

  const loadCart = useCallback(async () => {
    applyResolvedCart(await resolveCart());
  }, [applyResolvedCart]);

  useEffect(() => {
    let isCurrent = true;
    void resolveCart().then((resolved) => {
      if (isCurrent) applyResolvedCart(resolved);
    });
    return () => {
      isCurrent = false;
    };
  }, [applyResolvedCart]);

  const runMutation = useCallback(async (
    productId: string | null,
    remoteAction: () => Promise<Cart>,
    guestAction: () => Cart,
    fallbackMessage: string
  ) => {
    setPendingProductId(productId);
    try {
      setCart(userId ? await remoteAction() : guestAction());
    } catch (error) {
      if (error instanceof Error && !userId) throw error;
      throw new Error(getApiErrorMessage(error, fallbackMessage));
    } finally {
      setPendingProductId(null);
    }
  }, [userId]);

  const persistGuest = useCallback((updater: (items: Cart["items"]) => Cart["items"]): Cart => {
    const items = updater(readGuestItems());
    writeGuestItems(items);
    return buildCart(items);
  }, []);

  const addItem = useCallback((product: CartProductSnapshot, quantity: number) => runMutation(
    product.productId,
    () => addCartItem({ productId: product.productId, quantity }),
    () => persistGuest((items) => addGuestItem(items, product, quantity)),
    "No se pudo añadir el producto al carrito."
  ), [persistGuest, runMutation]);

  const updateQuantity = useCallback((productId: string, quantity: number) => runMutation(
    productId,
    () => updateCartItem(productId, { quantity }),
    () => persistGuest((items) => updateGuestItem(items, productId, quantity)),
    "No se pudo actualizar la cantidad."
  ), [persistGuest, runMutation]);

  const removeItem = useCallback((productId: string) => runMutation(
    productId,
    () => removeCartItem(productId),
    () => persistGuest((items) => removeGuestItem(items, productId)),
    "No se pudo quitar el producto del carrito."
  ), [persistGuest, runMutation]);

  const clear = useCallback(() => runMutation(
    null,
    () => clearRemoteCart(),
    () => persistGuest(() => []),
    "No se pudo vaciar el carrito."
  ), [persistGuest, runMutation]);

  const value = useMemo<CartContextType>(() => ({
    cart,
    isLoading,
    isAuthenticated: userId !== null,
    pendingProductId,
    addItem,
    updateQuantity,
    removeItem,
    clear,
    refresh: loadCart,
  }), [addItem, cart, clear, isLoading, loadCart, pendingProductId, removeItem, updateQuantity, userId]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}
