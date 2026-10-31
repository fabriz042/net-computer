"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import DataTable, { type DataTableColumn } from "@/app/components/ui/data-table";
import SearchInput from "@/app/components/ui/search-input";
import useDebounce from "@/app/hooks/use-debounce";
import { listAdminCarts } from "@/app/modules/users/cart/services/admin-cart.service";
import type { AdminCart, AdminCartListResponse } from "@/app/modules/users/cart/types/admin-cart.type";
import { formatDateTime, formatPrice } from "@/app/modules/orders/order/services/order-format";

const pageSize = 10;

const cartColumns: DataTableColumn<AdminCart>[] = [
  {
    id: "customer",
    label: "Cliente",
    render: (cart) => (
      <span className="flex min-w-0 flex-col">
        <span className="truncate font-medium">{cart.userName}</span>
        <span className="truncate text-sm text-[#71839d]">{cart.userEmail}</span>
      </span>
    ),
  },
  {
    id: "products",
    label: "Productos",
    width: "narrow",
    align: "center",
    render: (cart) => cart.lineCount,
  },
  {
    id: "units",
    label: "Unidades",
    width: "narrow",
    align: "center",
    render: (cart) => cart.itemCount,
  },
  {
    id: "total",
    label: "Total",
    width: "medium",
    render: (cart) => <span className="font-semibold">S/. {formatPrice(cart.total)}</span>,
  },
  {
    id: "lastActivity",
    label: "Última actividad",
    width: "medium",
    render: (cart) => <span className="text-[#647895]">{formatDateTime(cart.lastActivityAt)}</span>,
  },
];

export default function ActiveCartsTable() {
  const [page, setPage] = useState(1);
  const [searchValue, setSearchValue] = useState("");
  const debouncedSearch = useDebounce(searchValue.trim(), 250);
  const [response, setResponse] = useState<AdminCartListResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const requestIdRef = useRef(0);

  const loadCarts = useCallback(async (pageToLoad: number, search: string) => {
    const requestId = ++requestIdRef.current;
    setIsLoading(true);
    setHasError(false);
    try {
      const result = await listAdminCarts({ page: pageToLoad, per_page: pageSize, search: search || undefined });
      if (requestId === requestIdRef.current) setResponse(result);
    } catch {
      if (requestId === requestIdRef.current) {
        setResponse(null);
        setHasError(true);
      }
    } finally {
      if (requestId === requestIdRef.current) setIsLoading(false);
    }
  }, []);

  const previousSearchRef = useRef(debouncedSearch);

  useEffect(() => {
    if (page !== 1 && debouncedSearch !== previousSearchRef.current) {
      previousSearchRef.current = debouncedSearch;
      setPage(1);
      return;
    }

    previousSearchRef.current = debouncedSearch;
    void loadCarts(page, debouncedSearch);
  }, [debouncedSearch, loadCarts, page]);

  const carts = response?.data ?? [];
  const summary = isLoading
    ? "Cargando carritos..."
    : response
      ? `Mostrando ${carts.length} de ${response.meta.total_items} carritos con productos`
      : "No se pudieron cargar los carritos";

  return (
    <section className="mt-5">
      <p className="m-0 text-[#647895]">
        Clientes con productos en el carrito que aún no confirman su compra.
      </p>
      <div className="mt-4 max-w-md">
        <SearchInput
          id="cart-search"
          label="Buscar carritos por cliente"
          placeholder="Buscar por nombre o correo..."
          value={searchValue}
          onValueChange={setSearchValue}
        />
      </div>
      <DataTable
        columns={cartColumns}
        rows={isLoading ? [] : carts}
        getRowKey={(cart) => cart.userId}
        summary={summary}
        currentPage={page}
        pageCount={Math.max(response?.meta.total_pages ?? 1, 1)}
        emptyMessage={hasError ? "Error al consultar los carritos." : "No hay carritos activos."}
        loadingMessage="Cargando carritos..."
        isLoading={isLoading}
        layout="content"
        onPageChange={setPage}
      />
    </section>
  );
}
