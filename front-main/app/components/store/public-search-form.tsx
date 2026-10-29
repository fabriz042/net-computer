"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { FiSearch } from "react-icons/fi";
import Button from "@/app/components/ui/button";

export default function PublicSearchForm() {
  const router = useRouter();
  const [query, setQuery] = useState("");

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const searchTerm = query.trim();

    if (!searchTerm) return;

    router.push(`/productos/busqueda/${encodeURIComponent(searchTerm)}`);
  };

  return (
    <form
      className="
        col-span-2 row-start-3 flex h-[34px] w-full items-center gap-2 px-[11px]
        min-[901px]:w-[clamp(150px,20vw,220px)] min-[901px]:flex-none min-[1100px]:w-[clamp(190px,23vw,284px)]
        text-slate-500
        rounded-lg bg-[#f1f4f8]
      "
      onSubmit={handleSubmit}
      role="search"
    >
      <Button
        type="submit"
        variant="transparent"
        size="search"
        aria-label="Buscar"
      >
        <FiSearch aria-hidden="true" className="h-[14px] w-[14px]" />
      </Button>
      <input
        type="search"
        value={query}
        onChange={(event) => setQuery(event.currentTarget.value)}
        placeholder="Buscar productos..."
        aria-label="Buscar productos"
        className="
          w-full min-w-0 p-0
          text-[11px] text-[#1d2939] placeholder:text-slate-400
          border-0 bg-transparent outline-none
        "
      />
    </form>
  );
}
