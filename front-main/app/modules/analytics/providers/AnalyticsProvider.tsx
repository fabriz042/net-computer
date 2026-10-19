"use client";

import React, { createContext, useCallback, useEffect } from "react";
import { usePathname } from "next/navigation";
import { trackEvent } from "../services/analytics-tracker";
import { EventType } from "../types/analytics.interface";

export interface AnalyticsContextType {
  track: (eventType: EventType, metadata?: Record<string, unknown>) => void;
}

export const AnalyticsContext = createContext<AnalyticsContextType | null>(null);

interface AnalyticsProviderProps {
  children: React.ReactNode;
}

export function AnalyticsProvider({ children }: AnalyticsProviderProps) {
  const pathname = usePathname();

  useEffect(() => {
    if (pathname) {
      trackEvent("PAGE_VIEW", { page: pathname });
    }
  }, [pathname]);

  const track = useCallback(
    (eventType: EventType, metadata?: Record<string, unknown>) => {
      trackEvent(eventType, { page: pathname, ...metadata });
    },
    [pathname]
  );

  return (
    <AnalyticsContext.Provider value={{ track }}>
      {children}
    </AnalyticsContext.Provider>
  );
}
