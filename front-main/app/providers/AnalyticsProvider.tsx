"use client";

import React, { Suspense, createContext, useCallback, useEffect } from "react";
import { usePathname } from "next/navigation";
import { trackPublicEvent } from "@/app/modules/analytics/event/services/public-event-tracker";
import type { PublicEventType } from "@/app/modules/analytics/event/types/public-event.type";

export interface AnalyticsContextType {
  track: (eventType: PublicEventType, metadata?: Record<string, unknown>) => void;
}

export const AnalyticsContext = createContext<AnalyticsContextType | null>(null);

interface AnalyticsProviderProps {
  children: React.ReactNode;
}

function AnalyticsPageViewTracker() {
  const pathname = usePathname();

  useEffect(() => {
    if (pathname) {
      trackPublicEvent("PAGE_VIEW", { page: pathname });
    }
  }, [pathname]);

  return null;
}

export function AnalyticsProvider({ children }: AnalyticsProviderProps) {
  const track = useCallback((eventType: PublicEventType, metadata?: Record<string, unknown>) => {
    const page = typeof window === "undefined" ? undefined : window.location.pathname;
    trackPublicEvent(eventType, { page, ...metadata });
  }, []);

  return (
    <AnalyticsContext.Provider value={{ track }}>
      <Suspense fallback={null}>
        <AnalyticsPageViewTracker />
      </Suspense>
      {children}
    </AnalyticsContext.Provider>
  );
}
