"use client";

import Link from "next/link";
import { useEffect, useState, type ReactNode } from "react";
import { FiChevronRight, FiPackage } from "react-icons/fi";
import Breadcrumbs from "@/app/components/shared/breadcrumbs";
import Container from "@/app/components/shared/container";
import OrderStatusBadge from "@/app/components/shared/order-status-badge";
import PublicFooter from "@/app/components/layout/public/public-footer";
import PublicHeader from "@/app/components/layout/public/public-header";
import Button from "@/app/components/ui/button";
import { useCart } from "@/app/hooks/use-cart";
import { getApiErrorMessage } from "@/app/modules/api-error.service";
import { listOrders } from "@/app/modules/orders/order/services/order.service";
import { formatDateTime, formatPrice } from "@/app/modules/orders/order/services/order-format";
import type { OrderListResponse } from "@/app/modules/orders/order/types/order.type";

const pageSize = 10;

function StorePageShell({ children }: { children: ReactNode }) {
  return (
    <Container>
      <PublicHeader />
      <main className="min-h-[60vh] px-4 pb-12 pt-6 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <Breadcrumbs items={[{ label: "Inicio", href: "/" }, { label: "Mis pedidos" }]} />
        {children}
      </main>
      <PublicFooter />
    </Container>
  );
}

export default function OrdersPage() {
  const { isAuthenticated, isLoading: isSessionLoading } = useCart();
  const [page, setPage] = useState(1);
  const [reloadToken, setReloadToken] = useState(0);
  const [result, setResult] = useState<{
    key: string;
    response: OrderListResponse | null;
    error: string | null;
  } | null>(null);
  const requestKey = `${page}-${reloadToken}`;

  useEffect(() => {
    if (isSessionLoading || !isAuthenticated) return;
    let isCurrent = true;

    void listOrders({ page, per_page: pageSize })
      .then((response) => {
        if (isCurrent) setResult({ key: requestKey, response, error: null });
      })
      .catch((loadError: unknown) => {
        if (isCurrent) {
          setResult({
            key: requestKey,
            response: null,
            error: getApiErrorMessage(loadError, "No pudimos cargar tus pedidos."),
          });
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [isAuthenticated, isSessionLoading, page, requestKey]);

  const isLoading = result?.key !== requestKey;
  const response = result?.response ?? null;
  const error = isLoading ? null : result?.error ?? null;

  if (!isSessionLoading && !isAuthenticated) {
    return (
      <StorePageShell>
        <div className="flex min-h-[40vh] flex-col items-center justify-center gap-4 text-center">
          <h1 className="m-0 text-2xl font-bold text-[#111827]">Inicia sesión para ver tus pedidos</h1>
          <Button href="/login?redirect=/pedidos" size="large">Iniciar sesión</Button>
        </div>
      </StorePageShell>
    );
  }

  const orders = response?.data ?? [];
  const totalPages = Math.max(response?.meta.total_pages ?? 1, 1);

  return (
    <StorePageShell>
      <h1 className="m-0 text-3xl font-bold leading-tight text-[#111827]">Mis pedidos</h1>

      {error && (
        <div className="
          mt-4 flex flex-wrap items-center justify-between gap-3 px-4 py-3
          text-sm text-red-700
          rounded-md border border-red-200 bg-red-50
        " role="alert">
          <span>{error}</span>
          <button className="cursor-pointer font-semibold underline" type="button" onClick={() => setReloadToken((token) => token + 1)}>
            Reintentar
          </button>
        </div>
      )}

      {isLoading || isSessionLoading ? (
        <p className="mt-5 text-base text-[#647895]">Cargando pedidos...</p>
      ) : orders.length === 0 && !error ? (
        <div className="flex min-h-[35vh] flex-col items-center justify-center gap-4 text-center">
          <FiPackage aria-hidden="true" className="h-8 w-8 text-[#94a3b8]" />
          <p className="m-0 text-base text-[#647895]">Todavía no has realizado pedidos.</p>
          <Button href="/" size="large">Ver productos</Button>
        </div>
      ) : (
        <ul className="mt-5 m-0 list-none overflow-hidden p-0 rounded-lg border border-[#e0e6ef] bg-white">
          {orders.map((order) => (
            <li className="border-b border-[#edf1f6] last:border-b-0" key={order.id}>
              <Link
                className="
                  grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 px-4 py-4
                  sm:grid-cols-[minmax(0,1fr)_120px_140px_auto]
                  text-inherit no-underline
                  transition hover:bg-[#f8fafc]
                "
                href={`/pedidos/${order.id}`}
              >
                <span className="min-w-0">
                  <span className="block font-semibold text-[#111827]">Pedido #{order.code}</span>
                  <span className="block text-sm text-[#647895]">
                    {formatDateTime(order.createdAt)} · {order.itemCount} {order.itemCount === 1 ? "producto" : "productos"}
                  </span>
                </span>
                <span className="hidden sm:block">
                  <OrderStatusBadge status={order.status} label={order.statusName} />
                </span>
                <span className="hidden text-right font-bold text-[#111827] sm:block">
                  S/. {formatPrice(order.total)}
                </span>
                <FiChevronRight aria-hidden="true" className="h-4 w-4 text-[#94a3b8]" />
              </Link>
            </li>
          ))}
        </ul>
      )}

      {totalPages > 1 && (
        <nav className="mt-4 flex items-center justify-end gap-3" aria-label="Paginación de pedidos">
          <Button type="button" variant="outline" size="small" disabled={page <= 1 || isLoading} onClick={() => setPage(page - 1)}>
            Anterior
          </Button>
          <span className="text-sm text-[#647895]">Página {page} de {totalPages}</span>
          <Button type="button" variant="outline" size="small" disabled={page >= totalPages || isLoading} onClick={() => setPage(page + 1)}>
            Siguiente
          </Button>
        </nav>
      )}
    </StorePageShell>
  );
}
