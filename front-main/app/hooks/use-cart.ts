"use client";

import { useContext } from "react";
import { CartContext, CartContextType } from "@/app/providers/CartProvider";

export function useCart(): CartContextType {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
}
