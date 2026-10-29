"use client";

import { useEffect, useId, type MouseEvent, type ReactNode } from "react";
import { FiX } from "react-icons/fi";
import Button from "@/app/components/ui/button";

interface DialogProps {
  open: boolean;
  title: string;
  onClose: () => void;
  children?: ReactNode;
}

export default function Dialog({
  open,
  title,
  onClose,
  children,
}: DialogProps) {
  const titleId = useId();

  useEffect(() => {
    if (!open) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  const handleBackdropMouseDown = (event: MouseEvent<HTMLDivElement>) => {
    if (event.target === event.currentTarget) onClose();
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
      role="presentation"
      onMouseDown={handleBackdropMouseDown}
    >
      <section
        className="
          max-h-[90vh] w-full max-w-lg overflow-y-auto p-5
          text-[#111827]
          rounded-xl bg-white shadow-xl
        "
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
      >
        <header className="flex items-center justify-between gap-4">
          <h2 id={titleId} className="m-0 text-lg font-semibold">
            {title}
          </h2>
          <Button
            type="button"
            variant="transparent"
            size="icon"
            aria-label="Cerrar diálogo"
            onClick={onClose}
          >
            <FiX aria-hidden="true" className="h-5 w-5" />
          </Button>
        </header>
        {children}
      </section>
    </div>
  );
}
