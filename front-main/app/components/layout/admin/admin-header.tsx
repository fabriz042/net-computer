import Link from "next/link";
import { FiArrowLeft } from "react-icons/fi";

export default function AdminHeader() {
  return (
    <header className="
      flex min-h-[54px] items-center justify-between gap-4 px-4 sm:px-7
      bg-white
    ">
      <Link
        className="
          flex shrink-0 items-center gap-2
          text-[#111827] no-underline
        "
        href="/admin"
        aria-label="Net Computer, panel de administración"
      >
        <span className="text-base font-bold tracking-tight">
          NET COMPUTER
        </span>
        <span className="
          px-2 py-[5px]
          text-xs font-bold uppercase leading-none text-white
          rounded bg-[#111827]
        ">
          Admin panel
        </span>
      </Link>

      <div className="flex min-w-0 items-center gap-3 sm:gap-8">
        <Link
          className="
            inline-flex shrink-0 items-center gap-1
            text-sm font-medium text-blue-700 no-underline
            hover:text-blue-900
          "
          href="/"
        >
          <FiArrowLeft aria-hidden="true" className="h-3 w-3" />
          <span className="hidden sm:inline">Ver tienda online</span>
          <span className="sm:hidden">Tienda</span>
        </Link>
        <div className="flex items-center gap-2">
          <span className="
            grid h-7 w-7 shrink-0 place-items-center
            text-xs font-semibold text-white
            rounded-full bg-[#111827]
          ">
            AD
          </span>
          <span className="
            hidden min-w-0 flex-col sm:flex
            leading-tight
          ">
            <span className="text-xs text-[#647895]">Administrador</span>
            <span className="text-xs text-[#647895]">admin@netcomputer.pe</span>
          </span>
        </div>
      </div>
    </header>
  );
}
