"use client";

import { useContext } from "react";
import {
  AnalyticsContext,
  AnalyticsContextType,
} from "@/app/providers/AnalyticsProvider";

export function useAnalytics(): AnalyticsContextType {
  const context = useContext(AnalyticsContext);
  if (!context) {
    throw new Error("useAnalytics must be used within an AnalyticsProvider");
  }
  return context;
}
