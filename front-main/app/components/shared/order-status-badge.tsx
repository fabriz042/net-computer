import type { OrderStatusCode } from "@/app/modules/orders/order/types/order.type";

const statusStyles: Record<OrderStatusCode, string> = {
  PENDING: "text-[#92400e] bg-[#fef3c7]",
  PAID: "text-[#1e40af] bg-[#dbeafe]",
  PROCESSING: "text-[#6b21a8] bg-[#f3e8ff]",
  SHIPPED: "text-[#155e75] bg-[#cffafe]",
  DELIVERED: "text-[#166534] bg-[#dcfce7]",
  CANCELLED: "text-[#475569] bg-[#f1f5f9]",
};

interface OrderStatusBadgeProps {
  status: OrderStatusCode;
  label: string;
}

export default function OrderStatusBadge({ status, label }: OrderStatusBadgeProps) {
  return (
    <span className={`
      inline-flex min-w-[86px] items-center justify-center px-2.5 py-1
      text-xs font-semibold whitespace-nowrap
      rounded-full ${statusStyles[status]}
    `}>
      {label}
    </span>
  );
}
