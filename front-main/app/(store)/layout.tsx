import type { ReactNode } from "react";
import { AnalyticsProvider } from "@/app/providers/AnalyticsProvider";
import { CartProvider } from "@/app/providers/CartProvider";

export default function StoreLayout({ children }: { children: ReactNode }) {
  return (
    <AnalyticsProvider>
      <CartProvider>{children}</CartProvider>
    </AnalyticsProvider>
  );
}
