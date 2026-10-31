"use client";

import { useEffect, useState } from "react";
import Button from "@/app/components/ui/button";
import Dialog from "@/app/components/ui/dialog";
import OrderStatusBadge from "@/app/components/shared/order-status-badge";
import { getApiErrorMessage } from "@/app/modules/api-error.service";
import {
  getAdminOrder,
  updateAdminOrderStatus,
} from "@/app/modules/orders/order/services/admin-order.service";
import { formatDateTime, formatPrice } from "@/app/modules/orders/order/services/order-format";
import type { OrderStatusOption } from "@/app/modules/orders/order/types/admin-order.type";
import type { Order, OrderStatusCode } from "@/app/modules/orders/order/types/order.type";

interface OrderDetailDialogProps {
  orderId: string | null;
  statuses: OrderStatusOption[];
  onClose: () => void;
  onUpdated: (order: Order) => void;
}

export default function OrderDetailDialog({
  orderId,
  statuses,
  onClose,
  onUpdated,
}: OrderDetailDialogProps) {
  const [result, setResult] = useState<{
    id: string;
    order: Order | null;
    error: string | null;
  } | null>(null);
  const [nextStatus, setNextStatus] = useState<OrderStatusCode | "">("");
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    if (!orderId) return;
    let isCurrent = true;

    void getAdminOrder(orderId)
      .then((order) => {
        if (isCurrent) setResult({ id: orderId, order, error: null });
      })
      .catch((error: unknown) => {
        if (isCurrent) {
          setResult({
            id: orderId,
            order: null,
            error: getApiErrorMessage(error, "No se pudo cargar el pedido."),
          });
        }
      });

    return () => {
      isCurrent = false;
    };
  }, [orderId]);

  const isLoading = orderId !== null && result?.id !== orderId;
  const order = isLoading ? null : result?.order ?? null;
  const loadError = isLoading ? null : result?.error ?? null;

  const handleClose = () => {
    setNextStatus("");
    setSaveError(null);
    onClose();
  };

  const statusName = (code: OrderStatusCode): string =>
    statuses.find((status) => status.code === code)?.name ?? code;

  const handleSave = async () => {
    if (!order || !nextStatus) return;
    setIsSaving(true);
    setSaveError(null);
    try {
      const updatedOrder = await updateAdminOrderStatus(order.id, { status: nextStatus });
      setResult({ id: updatedOrder.id, order: updatedOrder, error: null });
      setNextStatus("");
      onUpdated(updatedOrder);
    } catch (error) {
      setSaveError(getApiErrorMessage(error, "No se pudo actualizar el estado del pedido."));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <Dialog
      open={orderId !== null}
      title={order ? `Pedido #${order.code}` : "Detalle del pedido"}
      onClose={handleClose}
    >
      {isLoading && <p className="mb-0 mt-4 text-[#647895]">Cargando pedido...</p>}
      {loadError && <p className="mb-0 mt-4 text-red-700" role="alert">{loadError}</p>}

      {order && (
        <div className="mt-4 grid gap-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <span className="text-sm text-[#647895]">{formatDateTime(order.createdAt)}</span>
            <OrderStatusBadge status={order.status} label={order.statusName} />
          </div>

          <section className="grid gap-1 p-3 text-sm rounded-md bg-[#f8fafc]">
            <span className="font-semibold text-[#111827]">{order.customerName}</span>
            <span className="text-[#647895]">{order.customerEmail}</span>
            <span className="text-[#526582]">{order.shippingAddress}</span>
          </section>

          <ul className="m-0 list-none overflow-hidden p-0 rounded-md border border-[#e0e6ef]">
            {order.items.map((item) => (
              <li
                className="flex items-center justify-between gap-3 px-3 py-2 text-sm border-b border-[#edf1f6] last:border-b-0"
                key={item.id}
              >
                <span className="min-w-0">
                  <span className="block font-medium text-[#111827]">{item.productName}</span>
                  <span className="block text-[#71839d]">{item.quantity} × S/. {formatPrice(item.unitPrice)}</span>
                </span>
                <span className="shrink-0 font-semibold text-[#111827]">S/. {formatPrice(item.subtotal)}</span>
              </li>
            ))}
            <li className="flex justify-between gap-3 px-3 py-2 font-bold text-[#111827] bg-[#f8fafc]">
              <span>Total</span>
              <span>S/. {formatPrice(order.total)}</span>
            </li>
          </ul>

          {order.nextStatuses.length > 0 ? (
            <section className="grid gap-2">
              <label className="text-sm font-semibold text-[#344054]" htmlFor="order-next-status">
                Cambiar estado
              </label>
              <div className="grid grid-cols-[minmax(0,1fr)_auto] gap-2">
                <select
                  className="
                    h-9 w-full px-3
                    text-sm text-[#111827]
                    rounded-md border border-[#d5deea] bg-white outline-none
                    focus:border-blue-500
                  "
                  id="order-next-status"
                  value={nextStatus}
                  onChange={(event) => setNextStatus(event.currentTarget.value as OrderStatusCode | "")}
                >
                  <option value="">Selecciona un estado</option>
                  {order.nextStatuses.map((code) => (
                    <option key={code} value={code}>{statusName(code)}</option>
                  ))}
                </select>
                <Button type="button" disabled={!nextStatus || isSaving} onClick={() => void handleSave()}>
                  {isSaving ? "Guardando..." : "Guardar"}
                </Button>
              </div>
              {nextStatus === "CANCELLED" && (
                <p className="m-0 text-sm text-[#9a3412]">
                  Al cancelar, las unidades vuelven al inventario.
                </p>
              )}
              {saveError && <p className="m-0 text-sm text-red-700" role="alert">{saveError}</p>}
            </section>
          ) : (
            <p className="m-0 text-sm text-[#647895]">Este pedido ya no admite cambios de estado.</p>
          )}
        </div>
      )}
    </Dialog>
  );
}
