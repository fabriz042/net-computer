"use client";

import type { IconType } from "react-icons";
import { FiClock, FiDollarSign, FiPackage, FiShoppingCart, FiXCircle } from "react-icons/fi";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatPrice, formatShortDate } from "@/app/modules/orders/order/services/order-format";
import type { SalesSummary } from "@/app/modules/orders/order/types/admin-order.type";

interface SalesOverviewProps {
  summary: SalesSummary | null;
  isLoading: boolean;
  hasError: boolean;
}

function MetricCard({
  icon: Icon,
  label,
  value,
  detail,
}: {
  icon: IconType;
  label: string;
  value: string;
  detail: string;
}) {
  return (
    <div className="flex min-w-0 flex-col gap-2 p-4 rounded-lg border border-[#dce4ef] bg-white">
      <span className="flex items-center gap-2 text-sm font-medium text-[#647895]">
        <Icon aria-hidden="true" className="h-4 w-4 shrink-0" />
        {label}
      </span>
      <span className="truncate text-2xl font-bold tracking-tight text-[#111827]">{value}</span>
      <span className="text-sm text-[#71839d]">{detail}</span>
    </div>
  );
}

export default function SalesOverview({ summary, isLoading, hasError }: SalesOverviewProps) {
  if (isLoading && !summary) {
    return <p className="mb-0 mt-5 text-[#647895]">Cargando resumen de ventas...</p>;
  }

  if (hasError || !summary) {
    return <p className="mb-0 mt-5 text-red-700">No se pudo cargar el resumen de ventas.</p>;
  }

  const hasSales = summary.dailySales.some((day) => day.orderCount > 0);

  return (
    <div className={`mt-5 grid gap-4 ${isLoading ? "opacity-60" : ""}`}>
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-5">
        <MetricCard
          icon={FiDollarSign}
          label="Ingresos"
          value={`S/. ${formatPrice(summary.totalRevenue)}`}
          detail="Pedidos pagados o posteriores"
        />
        <MetricCard
          icon={FiShoppingCart}
          label="Ventas"
          value={String(summary.completedOrders)}
          detail={`Ticket promedio S/. ${formatPrice(summary.averageTicket)}`}
        />
        <MetricCard
          icon={FiPackage}
          label="Unidades vendidas"
          value={String(summary.unitsSold)}
          detail="Productos despachables"
        />
        <MetricCard
          icon={FiClock}
          label="Pendientes de pago"
          value={String(summary.pendingOrders)}
          detail={`S/. ${formatPrice(summary.pendingAmount)} por confirmar`}
        />
        <MetricCard
          icon={FiXCircle}
          label="Cancelados"
          value={String(summary.cancelledOrders)}
          detail="Stock devuelto al inventario"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[minmax(0,1fr)_340px]">
        <section className="min-w-0 p-5 rounded-lg border border-[#dce4ef] bg-white">
          <h2 className="m-0 font-semibold text-[#111827]">Ingresos por día</h2>
          {hasSales ? (
            <div className="mt-4 w-full">
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={summary.dailySales} margin={{ top: 8, right: 8, bottom: 0, left: 8 }}>
                  <CartesianGrid stroke="#e5eaf1" strokeDasharray="4 4" vertical={false} />
                  <XAxis
                    dataKey="date"
                    tickFormatter={formatShortDate}
                    tick={{ fill: "#647895", fontSize: 12 }}
                    minTickGap={16}
                  />
                  <YAxis
                    tick={{ fill: "#647895", fontSize: 12 }}
                    tickFormatter={(value: number) => `S/. ${Math.round(value)}`}
                    width={84}
                  />
                  <Tooltip
                    cursor={{ fill: "#f1f5f9" }}
                    labelFormatter={(label) => formatShortDate(String(label))}
                    formatter={(value) => [`S/. ${formatPrice(Number(value))}`, "Ingresos"]}
                  />
                  <Bar dataKey="revenue" fill="#111827" maxBarSize={28} radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <p className="mb-0 mt-4 text-[#647895]">No hay ventas confirmadas en este periodo.</p>
          )}
        </section>

        <section className="p-5 rounded-lg border border-[#dce4ef] bg-white">
          <h2 className="m-0 font-semibold text-[#111827]">Productos más vendidos</h2>
          {summary.topProducts.length === 0 ? (
            <p className="mb-0 mt-4 text-[#647895]">Aún no hay productos vendidos.</p>
          ) : (
            <ol className="mb-0 mt-4 grid list-none gap-3 p-0">
              {summary.topProducts.map((product, index) => (
                <li className="grid grid-cols-[24px_minmax(0,1fr)_auto] items-center gap-3" key={product.productId}>
                  <span className="grid h-6 w-6 place-items-center text-xs font-bold text-white rounded-full bg-[#111827]">
                    {index + 1}
                  </span>
                  <span className="min-w-0">
                    <span className="block truncate text-sm font-medium text-[#111827]">{product.productName}</span>
                    <span className="block text-xs text-[#71839d]">{product.unitsSold} {product.unitsSold === 1 ? "unidad" : "unidades"}</span>
                  </span>
                  <span className="text-sm font-semibold text-[#111827]">S/. {formatPrice(product.revenue)}</span>
                </li>
              ))}
            </ol>
          )}
        </section>
      </div>
    </div>
  );
}
