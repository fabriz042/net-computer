"use client";

import Link from "next/link";
import { useParams, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState, type ReactNode } from "react";
import { FiCheckCircle, FiXCircle } from "react-icons/fi";
import Breadcrumbs from "@/app/components/shared/breadcrumbs";
import Container from "@/app/components/shared/container";
import OrderStatusBadge from "@/app/components/shared/order-status-badge";
import PublicFooter from "@/app/components/layout/public/public-footer";
import PublicHeader from "@/app/components/layout/public/public-header";
import Button from "@/app/components/ui/button";
import Snackbar from "@/app/components/ui/snackbar";
import { getApiErrorMessage } from "@/app/modules/api-error.service";
import { cancelOrder, getOrder } from "@/app/modules/orders/order/services/order.service";
import { formatDateTime, formatPrice } from "@/app/modules/orders/order/services/order-format";
import type { Order } from "@/app/modules/orders/order/types/order.type";

function StorePageShell({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Container>
      <PublicHeader />
      <main className="min-h-[60vh] px-4 pb-12 pt-6 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <Breadcrumbs
          items={[
            { label: "Inicio", href: "/" },
            { label: "Mis pedidos", href: "/pedidos" },
            { label: title },
          ]}
        />
        {children}
      </main>
      <PublicFooter />
    </Container>
  );
}

function OrderDetailContent() {
  const { id } = useParams<{ id: string }>();
  const searchParams = useSearchParams();
  const isNewOrder = searchParams.get("nuevo") === "1";
  const [result, setResult] = useState<{
    id: string;
    order: Order | null;
    error: string | null;
  } | null>(null);
  const [isCancelling, setIsCancelling] = useState(false);
  const [cancelError, setCancelError] = useState<string | null>(null);

  useEffect(() => {
    let isCurrent = true;

    void getOrder(id)
      .then((order) => {
        if (isCurrent) setResult({ id, order, error: null });
      })
      .catch((error: unknown) => {
        if (isCurrent) {
          setResult({ id, order: null, error: getApiErrorMessage(error, "No pudimos cargar el pedido.") });
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [id]);

  const isLoading = result?.id !== id;
  const order = isLoading ? null : result?.order ?? null;
  const loadError = isLoading ? null : result?.error ?? null;

  const handleCancel = async () => {
    if (!order) return;
    setIsCancelling(true);
    setCancelError(null);
    try {
      const cancelledOrder = await cancelOrder(order.id);
      setResult({ id, order: cancelledOrder, error: null });
    } catch (error) {
      setCancelError(getApiErrorMessage(error, "No se pudo cancelar el pedido."));
    } finally {
      setIsCancelling(false);
    }
  };

  if (isLoading) {
    return (
      <StorePageShell title="Pedido">
        <p className="m-0 text-base text-[#647895]">Cargando pedido...</p>
      </StorePageShell>
    );
  }

  if (loadError || !order) {
    return (
      <StorePageShell title="Pedido">
        <div className="flex min-h-[40vh] flex-col items-center justify-center gap-4 text-center">
          <h1 className="m-0 text-2xl font-bold text-[#111827]">No encontramos este pedido</h1>
          <p className="m-0 text-base text-[#647895]">{loadError}</p>
          <Button href="/pedidos" variant="outline" size="large">Ver mis pedidos</Button>
        </div>
      </StorePageShell>
    );
  }

  const canCancel = order.status === "PENDING";

  return (
    <StorePageShell title={`Pedido #${order.code}`}>
      {cancelError && <Snackbar key={cancelError} type="error" text={cancelError} />}

      {isNewOrder && order.status !== "CANCELLED" && (
        <section className="
          mb-6 flex items-start gap-3 p-4
          text-[#166534]
          rounded-lg border border-green-200 bg-green-50
        " role="status">
          <FiCheckCircle aria-hidden="true" className="mt-0.5 h-6 w-6 shrink-0" />
          <div>
            <h2 className="m-0 text-lg font-bold">¡Gracias por tu compra!</h2>
            <p className="mb-0 mt-1 text-base">
              Registramos tu pedido #{order.code}. Te contactaremos para coordinar el pago y el envío.
            </p>
          </div>
        </section>
      )}

      {order.status === "CANCELLED" && (
        <section className="
          mb-6 flex items-center gap-3 p-4
          text-[#475569]
          rounded-lg border border-slate-200 bg-slate-50
        " role="status">
          <FiXCircle aria-hidden="true" className="h-5 w-5 shrink-0" />
          <p className="m-0 text-base">Este pedido fue cancelado.</p>
        </section>
      )}

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="m-0 text-3xl font-bold leading-tight text-[#111827]">Pedido #{order.code}</h1>
          <p className="mb-0 mt-1 text-base text-[#647895]">Realizado el {formatDateTime(order.createdAt)}</p>
        </div>
        <OrderStatusBadge status={order.status} label={order.statusName} />
      </div>

      <div className="mt-6 grid grid-cols-1 items-start gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
        <section className="overflow-hidden rounded-lg border border-[#e0e6ef] bg-white">
          <h2 className="m-0 px-4 py-3 text-base font-bold text-[#111827] border-b border-[#e0e6ef] bg-[#f8fafc]">
            Productos
          </h2>
          <ul className="m-0 list-none p-0">
            {order.items.map((item) => (
              <li
                className="flex items-center justify-between gap-4 px-4 py-3 border-b border-[#edf1f6] last:border-b-0"
                key={item.id}
              >
                <div className="min-w-0">
                  <p className="m-0 font-semibold text-[#111827]">{item.productName}</p>
                  <p className="mb-0 mt-0.5 text-sm text-[#647895]">
                    {item.quantity} × S/. {formatPrice(item.unitPrice)}
                  </p>
                </div>
                <span className="shrink-0 font-bold text-[#111827]">S/. {formatPrice(item.subtotal)}</span>
              </li>
            ))}
          </ul>
          <div className="flex justify-between gap-4 px-4 py-3 text-lg font-bold text-[#111827] border-t border-[#e0e6ef]">
            <span>Total</span>
            <span>S/. {formatPrice(order.total)}</span>
          </div>
        </section>

        <aside className="grid gap-4">
          <section className="p-4 rounded-lg border border-[#e0e6ef] bg-[#f8fafc]">
            <h2 className="m-0 text-base font-bold text-[#111827]">Envío</h2>
            <p className="mb-0 mt-2 text-base text-[#526582]">{order.shippingAddress}</p>
            <p className="mb-0 mt-3 text-sm text-[#71839d]">
              {order.customerName} · {order.customerEmail}
            </p>
          </section>
          {canCancel && (
            <Button
              type="button"
              variant="outline"
              size="large"
              disabled={isCancelling}
              onClick={() => void handleCancel()}
            >
              {isCancelling ? "Cancelando..." : "Cancelar pedido"}
            </Button>
          )}
          <Button href="/" size="large">Seguir comprando</Button>
          <Link className="text-center text-sm font-medium text-blue-700 no-underline hover:text-blue-900" href="/pedidos">
            Ver todos mis pedidos
          </Link>
        </aside>
      </div>
    </StorePageShell>
  );
}

export default function OrderDetailPage() {
  return (
    <Suspense fallback={(
      <StorePageShell title="Pedido">
        <p className="m-0 text-base text-[#647895]">Cargando pedido...</p>
      </StorePageShell>
    )}>
      <OrderDetailContent />
    </Suspense>
  );
}
