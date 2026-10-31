"use client";

import { useContext } from "react";
import { FiShoppingBag } from "react-icons/fi";
import Button from "@/app/components/ui/button";
import { CartContext } from "@/app/providers/CartProvider";

export default function CartButton() {
  const cartContext = useContext(CartContext);
  const itemCount = cartContext?.cart.itemCount ?? 0;
  const label = itemCount === 1
    ? "Carrito de compras, 1 producto"
    : `Carrito de compras, ${itemCount} productos`;

  return (
    <Button
      href="/carrito"
      variant="outline"
      size="icon"
      aria-label={label}
    >
      <FiShoppingBag aria-hidden="true" className="h-4 w-4" />
      {itemCount > 0 && (
        <span className="
          absolute -right-1 -top-[5px] grid h-4 min-w-4 place-items-center px-1
          text-[9px] font-semibold leading-none text-white
          rounded-full bg-[#111827]
        ">
          {itemCount > 99 ? "99+" : itemCount}
        </span>
      )}
    </Button>
  );
}
