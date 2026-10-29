import type { ReactNode } from "react";
import { AnalyticsProvider } from "@/app/providers/AnalyticsProvider";

export default function StoreLayout({ children }: { children: ReactNode }) {
  return <AnalyticsProvider>{children}</AnalyticsProvider>;
}
