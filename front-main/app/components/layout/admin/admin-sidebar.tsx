"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  FiArchive,
  FiBox,
  FiShoppingBag,
  FiTag,
  FiAward,
  FiBarChart2,
  FiUsers,
} from "react-icons/fi";

const adminNavigation = [
  { label: "Búsquedas", href: "/admin", icon: FiBarChart2 },
  { label: "Productos", href: "/admin/productos", icon: FiBox },
  { label: "Categorías", href: "/admin/categorias", icon: FiTag },
  { label: "Marcas", href: "/admin/marcas", icon: FiAward },
  { label: "Inventario", href: "/admin/inventario", icon: FiArchive },
  { label: "Ventas", href: "/admin/ventas", icon: FiShoppingBag, count: "18" },
  { label: "Usuarios", href: "/admin/usuarios", icon: FiUsers },
];

export default function AdminSidebar() {
  const pathname = usePathname();

  return (
    <aside className="
      w-full shrink-0 md:w-56
      bg-white
    ">
      <p className="
        hidden px-5 pb-2 pt-6 md:block
        text-xs font-semibold uppercase tracking-wide text-[#94a3b8]
      ">
        Gestión del sistema
      </p>
      <nav
        className="
          flex gap-1 p-2 md:flex-col md:gap-1 md:px-3 md:pb-4 md:pt-1
          overflow-x-auto
        "
        aria-label="Administración"
      >
        {adminNavigation.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;
          const textColor = isActive ? "text-white" : "text-[#526582]";
          const surface = isActive ? "bg-[#111827]" : "hover:bg-slate-50";
          const hoverText = isActive ? "" : "hover:text-[#111827]";

          return (
            <Link
              className={`
                flex min-h-[34px] shrink-0 items-center gap-2 px-3 md:w-full
                text-sm font-medium ${textColor} no-underline
                rounded-md ${surface}
                transition ${hoverText}
              `}
              href={item.href}
              key={item.label}
              aria-current={isActive ? "page" : undefined}
            >
              <Icon aria-hidden="true" className="h-[13px] w-[13px] shrink-0" />
              <span>{item.label}</span>
              {item.count ? (
                <span className="
                  ml-auto px-1.5 py-0.5
                  text-xs font-semibold leading-none
                  rounded-full bg-white/10
                ">
                  {item.count}
                </span>
              ) : null}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
