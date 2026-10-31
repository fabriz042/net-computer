"use client";

import { Suspense, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { FiDownload, FiEye, FiRefreshCw } from "react-icons/fi";
import Button from "@/app/components/ui/button";
import DataTable, { type DataTableColumn } from "@/app/components/ui/data-table";
import SearchInput from "@/app/components/ui/search-input";
import Snackbar, { type SnackbarType } from "@/app/components/ui/snackbar";
import OrderStatusBadge from "@/app/components/shared/order-status-badge";
import ActiveCartsTable from "@/app/admin/ventas/components/active-carts-table";
import OrderDetailDialog from "@/app/admin/ventas/components/order-detail-dialog";
import SalesOverview from "@/app/admin/ventas/components/sales-overview";
import useDebounce from "@/app/hooks/use-debounce";
import { getApiErrorMessage } from "@/app/modules/api-error.service";
import {
  exportAdminOrders,
  getAdminSalesSummary,
  listAdminOrders,
  listAdminOrderStatuses,
} from "@/app/modules/orders/order/services/admin-order.service";
import {
  formatDate,
  formatDateTime,
  formatPrice,
  toStoreDateInput,
} from "@/app/modules/orders/order/services/order-format";
import type {
  AdminOrderFilters,
  OrderStatusOption,
  SalesSummary,
} from "@/app/modules/orders/order/types/admin-order.type";
import type {
  Order,
  OrderListResponse,
  OrderStatusCode,
} from "@/app/modules/orders/order/types/order.type";

type SalesTab = "orders" | "carts";

const pageSize = 10;

const filterControlClassName = `
  h-9 w-full px-3
  text-sm text-[#111827]
  rounded-md border border-[#d5deea] bg-white outline-none
  focus:border-blue-500
`;

const downloadBlob = (blob: Blob, fileName: string) => {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
};

function AdminSalesContent() {
  const [activeTab, setActiveTab] = useState<SalesTab>("orders");
  const [page, setPage] = useState(1);
  const [searchValue, setSearchValue] = useState("");
  const debouncedSearch = useDebounce(searchValue.trim(), 250);
  const [status, setStatus] = useState<OrderStatusCode | "">("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [statuses, setStatuses] = useState<OrderStatusOption[]>([]);
  const [response, setResponse] = useState<OrderListResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [summaryReloadToken, setSummaryReloadToken] = useState(0);
  const [summaryResult, setSummaryResult] = useState<{
    key: string;
    summary: SalesSummary | null;
  } | null>(null);
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);
  const [isExporting, setIsExporting] = useState(false);
  const [snackbar, setSnackbar] = useState<{ type: SnackbarType; text: string } | null>(null);
  const requestIdRef = useRef(0);

  const isDateRangeInvalid = fromDate !== "" && toDate !== "" && fromDate > toDate;
  const filters = useMemo<AdminOrderFilters>(() => ({
    search: debouncedSearch || undefined,
    status: status || undefined,
    from: isDateRangeInvalid ? undefined : fromDate || undefined,
    to: isDateRangeInvalid ? undefined : toDate || undefined,
  }), [debouncedSearch, fromDate, isDateRangeInvalid, status, toDate]);
  const filtersKey = JSON.stringify(filters);
  const previousFiltersKeyRef = useRef(filtersKey);

  const loadOrders = useCallback(async (pageToLoad: number, activeFilters: AdminOrderFilters) => {
    const requestId = ++requestIdRef.current;
    setIsLoading(true);
    setHasError(false);
    try {
      const result = await listAdminOrders({
        ...activeFilters,
        order: "desc",
        page: pageToLoad,
        per_page: pageSize,
      });
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

  useEffect(() => {
    void listAdminOrderStatuses()
      .then(setStatuses)
      .catch(() => setStatuses([]));
  }, []);

  useEffect(() => {
    if (page !== 1 && filtersKey !== previousFiltersKeyRef.current) {
      previousFiltersKeyRef.current = filtersKey;
      setPage(1);
      return;
    }

    previousFiltersKeyRef.current = filtersKey;
    void loadOrders(page, filters);
  }, [filters, filtersKey, loadOrders, page]);

  const summaryFrom = filters.from;
  const summaryTo = filters.to;
  const summaryKey = `${summaryFrom ?? ""}|${summaryTo ?? ""}|${summaryReloadToken}`;

  useEffect(() => {
    let isCurrent = true;

    void getAdminSalesSummary({ from: summaryFrom, to: summaryTo })
      .then((summary) => {
        if (isCurrent) setSummaryResult({ key: summaryKey, summary });
      })
      .catch(() => {
        if (isCurrent) setSummaryResult({ key: summaryKey, summary: null });
      });

    return () => {
      isCurrent = false;
    };
  }, [summaryFrom, summaryKey, summaryTo]);

  const isSummaryLoading = summaryResult?.key !== summaryKey;
  const hasSummaryError = !isSummaryLoading && summaryResult?.summary === null;
  const summary = summaryResult?.summary ?? null;

  const handleOrderUpdated = useCallback((order: Order) => {
    setResponse((current) => current
      ? { ...current, data: current.data.map((item) => item.id === order.id ? order : item) }
      : current);
    setSnackbar({ type: "success", text: `Pedido #${order.code} actualizado a ${order.statusName}.` });
    setSummaryReloadToken((token) => token + 1);
  }, []);

  const handleExport = async () => {
    setIsExporting(true);
    setSnackbar(null);
    try {
      const blob = await exportAdminOrders(filters);
      downloadBlob(blob, `reporte-ventas-${toStoreDateInput(new Date())}.xlsx`);
      setSnackbar({ type: "success", text: "Reporte de ventas descargado." });
    } catch (error) {
      setSnackbar({
        type: "error",
        text: getApiErrorMessage(error, "No se pudo generar el reporte de ventas."),
      });
    } finally {
      setIsExporting(false);
    }
  };

  const handleRefresh = () => {
    void loadOrders(page, filters);
    setSummaryReloadToken((token) => token + 1);
  };

  const clearFilters = () => {
    setSearchValue("");
    setStatus("");
    setFromDate("");
    setToDate("");
  };

  const orderColumns: DataTableColumn<Order>[] = [
    {
      id: "code",
      label: "Pedido",
      width: "narrow",
      render: (order) => <span className="font-mono font-semibold">#{order.code}</span>,
    },
    {
      id: "date",
      label: "Fecha",
      width: "medium",
      render: (order) => <span className="text-[#647895]">{formatDateTime(order.createdAt)}</span>,
    },
    {
      id: "customer",
      label: "Cliente",
      render: (order) => (
        <span className="flex min-w-0 flex-col">
          <span className="truncate font-medium">{order.customerName}</span>
          <span className="truncate text-sm text-[#71839d]">{order.customerEmail}</span>
        </span>
      ),
    },
    {
      id: "units",
      label: "Unid.",
      width: "compact",
      align: "center",
      render: (order) => order.itemCount,
    },
    {
      id: "total",
      label: "Total",
      width: "normal",
      render: (order) => <span className="font-semibold">S/. {formatPrice(order.total)}</span>,
    },
    {
      id: "status",
      label: "Estado",
      width: "normal",
      render: (order) => <OrderStatusBadge status={order.status} label={order.statusName} />,
    },
    {
      id: "actions",
      label: "Ver",
      width: "compact",
      align: "center",
      render: (order) => (
        <button
          className="
            inline-flex h-7 w-7 items-center justify-center
            text-[#647895]
            rounded transition hover:bg-slate-100 hover:text-[#111827]
          "
          type="button"
          aria-label={`Ver detalle del pedido ${order.code}`}
          onClick={() => setSelectedOrderId(order.id)}
        >
          <FiEye aria-hidden="true" className="h-4 w-4" />
        </button>
      ),
    },
  ];

  const orders = response?.data ?? [];
  const tableSummary = isLoading
    ? "Cargando pedidos..."
    : response
      ? `Mostrando ${orders.length} de ${response.meta.total_items} pedidos`
      : "No se pudieron cargar los pedidos";
  const hasActiveFilters = Boolean(searchValue || status || fromDate || toDate);
  const today = toStoreDateInput(new Date());

  return (
    <div className="mx-auto flex min-h-[calc(100vh-126px)] w-full max-w-[1200px] flex-col">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <div>
          <h1 className="m-0 text-2xl font-bold leading-tight tracking-tight text-[#111827]">
            Gestión de Ventas
          </h1>
          {summary && (
            <p className="mb-0 mt-2 text-[#647895]">
              Resumen del {formatDate(summary.from)} al {formatDate(summary.to)}
            </p>
          )}
        </div>
        <div className="flex flex-wrap items-center gap-2 self-start">
          <Button type="button" variant="outline" onClick={handleRefresh} disabled={isLoading}>
            <FiRefreshCw aria-hidden="true" className="h-3.5 w-3.5" />
            Actualizar
          </Button>
          <Button type="button" onClick={() => void handleExport()} disabled={isExporting}>
            <FiDownload aria-hidden="true" className="h-3.5 w-3.5" />
            {isExporting ? "Generando..." : "Exportar Excel"}
          </Button>
        </div>
      </div>

      {snackbar && (
        <Snackbar key={`${snackbar.type}-${snackbar.text}`} type={snackbar.type} text={snackbar.text} />
      )}

      <SalesOverview summary={summary} isLoading={isSummaryLoading} hasError={hasSummaryError} />

      <div className="mt-6 flex gap-1 border-b border-[#e0e6ef]" role="tablist" aria-label="Secciones de ventas">
        {([
          { id: "orders", label: "Pedidos" },
          { id: "carts", label: "Carritos activos" },
        ] as const).map((tab) => (
          <button
            aria-selected={activeTab === tab.id}
            className={`
              -mb-px px-4 py-2
              text-sm font-semibold ${activeTab === tab.id ? "text-[#111827]" : "text-[#647895]"}
              border-b-2 ${activeTab === tab.id ? "border-[#111827]" : "border-transparent"}
              transition hover:text-[#111827]
            `}
            key={tab.id}
            role="tab"
            type="button"
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === "orders" ? (
        <>
          <div className="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-[minmax(0,1fr)_170px_150px_150px_auto]">
            <SearchInput
              id="order-search"
              label="Buscar pedidos por cliente, correo o número"
              placeholder="Buscar por cliente, correo o N° de pedido..."
              value={searchValue}
              onValueChange={setSearchValue}
            />
            <select
              aria-label="Filtrar por estado"
              className={filterControlClassName}
              value={status}
              onChange={(event) => setStatus(event.currentTarget.value as OrderStatusCode | "")}
            >
              <option value="">Todos los estados</option>
              {statuses.map((option) => (
                <option key={option.code} value={option.code}>{option.name}</option>
              ))}
            </select>
            <input
              aria-label="Desde"
              className={filterControlClassName}
              max={toDate || today}
              type="date"
              value={fromDate}
              onChange={(event) => setFromDate(event.currentTarget.value)}
            />
            <input
              aria-label="Hasta"
              className={filterControlClassName}
              max={today}
              min={fromDate || undefined}
              type="date"
              value={toDate}
              onChange={(event) => setToDate(event.currentTarget.value)}
            />
            <Button
              type="button"
              variant="transparent"
              disabled={!hasActiveFilters}
              onClick={clearFilters}
            >
              Limpiar
            </Button>
          </div>

          {isDateRangeInvalid && (
            <p className="mb-0 mt-3 text-sm text-red-700" role="alert">
              La fecha inicial no puede ser posterior a la fecha final.
            </p>
          )}

          {hasError && (
            <div className="
              mt-4 flex flex-wrap items-center justify-between gap-3 px-4 py-3
              text-sm text-red-700
              rounded-md border border-red-200 bg-red-50
            " role="alert">
              <span>No se pudieron cargar los pedidos.</span>
              <button
                className="cursor-pointer font-semibold underline"
                type="button"
                onClick={() => void loadOrders(page, filters)}
              >
                Reintentar
              </button>
            </div>
          )}

          <DataTable
            columns={orderColumns}
            rows={isLoading ? [] : orders}
            getRowKey={(order) => order.id}
            summary={tableSummary}
            currentPage={page}
            pageCount={Math.max(response?.meta.total_pages ?? 1, 1)}
            emptyMessage={hasError
              ? "Error al consultar los pedidos."
              : hasActiveFilters
                ? "No hay pedidos que coincidan con los filtros."
                : "Aún no hay pedidos registrados."}
            loadingMessage="Cargando pedidos..."
            isLoading={isLoading}
            onPageChange={setPage}
          />
        </>
      ) : (
        <ActiveCartsTable />
      )}

      <OrderDetailDialog
        orderId={selectedOrderId}
        statuses={statuses}
        onClose={() => setSelectedOrderId(null)}
        onUpdated={handleOrderUpdated}
      />
    </div>
  );
}

export default function AdminSalesPage() {
  return (
    <Suspense fallback={<p className="m-0 text-[#647895]">Cargando ventas...</p>}>
      <AdminSalesContent />
    </Suspense>
  );
}
