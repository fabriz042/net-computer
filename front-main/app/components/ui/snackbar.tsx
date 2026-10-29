"use client";

import { useEffect, useState } from "react";
import { FiAlertTriangle, FiCheckCircle, FiXCircle } from "react-icons/fi";

export type SnackbarType = "success" | "warning" | "error";

interface SnackbarProps {
  type: SnackbarType;
  text: string;
}

const snackbarStyles: Record<SnackbarType, {
  icon: typeof FiCheckCircle;
  textColor: string;
  surface: string;
}> = {
  success: {
    icon: FiCheckCircle,
    textColor: "text-green-800",
    surface: "border-green-200 bg-green-50",
  },
  warning: {
    icon: FiAlertTriangle,
    textColor: "text-amber-900",
    surface: "border-amber-200 bg-amber-50",
  },
  error: {
    icon: FiXCircle,
    textColor: "text-red-800",
    surface: "border-red-200 bg-red-50",
  },
};

export default function Snackbar({ type, text }: SnackbarProps) {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    const timeout = window.setTimeout(() => setIsVisible(false), 5000);
    return () => window.clearTimeout(timeout);
  }, []);

  if (!isVisible) return null;

  const { icon: Icon, textColor, surface } = snackbarStyles[type];

  return (
    <div
      className={`
        fixed inset-x-4 bottom-4 z-[100] mx-auto flex w-full max-w-sm items-start gap-3 px-4 py-3
        sm:left-auto sm:right-6 sm:bottom-6
        ${textColor}
        rounded-lg border shadow-lg ${surface}
      `}
      role={type === "error" ? "alert" : "status"}
      aria-live={type === "error" ? "assertive" : "polite"}
      aria-atomic="true"
    >
      <Icon aria-hidden="true" className="mt-0.5 h-5 w-5 shrink-0" />
      <p className="min-w-0 flex-1">{text}</p>
    </div>
  );
}
