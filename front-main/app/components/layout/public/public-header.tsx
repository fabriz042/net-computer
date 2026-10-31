import Link from "next/link";
import { FiUser } from "react-icons/fi";
import Button from "@/app/components/ui/button";
import CartButton from "@/app/components/store/cart-button";
import PublicSearchForm from "@/app/components/store/public-search-form";

export default function PublicHeader() {
  return (
    <header className="
      grid grid-cols-[minmax(0,1fr)_auto] items-center gap-x-[18px] gap-y-3 px-4 py-3
      min-[901px]:flex min-[901px]:h-[59px] min-[901px]:gap-5 min-[901px]:px-[clamp(24px,4.1vw,50px)] min-[901px]:py-0 min-[1100px]:gap-[30px]
      border-b border-[#e0e6ef] bg-white
    ">
      <Link
        className="
          col-start-1 row-start-1 whitespace-nowrap
          text-[18px] font-bold leading-none tracking-[-0.045em] text-[#111827]
          no-underline
        "
        href="/"
        aria-label="Net Computer, inicio"
      >
        NET COMPUTER
      </Link>

      <nav
        className="
          col-span-2 row-start-2 flex items-center justify-between gap-3 pt-1
          min-[901px]:min-w-[190px] min-[901px]:flex-1 min-[901px]:justify-center min-[901px]:gap-5 min-[901px]:pt-0 min-[1100px]:min-w-[220px] min-[1100px]:gap-[clamp(20px,3.8vw,46px)]
        "
        aria-label="Categorías principales"
      >
        <Link
          className="
            whitespace-nowrap
            text-xs font-medium text-[#344054]
            no-underline transition hover:text-[#111827]
          "
          href="/productos/laptops"
        >
          Laptops
        </Link>
        <Link
          className="
            whitespace-nowrap
            text-xs font-medium text-[#344054]
            no-underline transition hover:text-[#111827]
          "
          href="/productos/monitores"
        >
          Monitores
        </Link>
        <Link
          className="
            whitespace-nowrap
            text-xs font-medium text-[#344054]
            no-underline transition hover:text-[#111827]
          "
          href="/productos/teclados"
        >
          Teclados
        </Link>
        <Link
          className="
            whitespace-nowrap
            text-xs font-medium text-[#344054]
            no-underline transition hover:text-[#111827]
          "
          href="/productos/mouse"
        >
          Mouse
        </Link>
        <Link
          className="
            whitespace-nowrap
            text-xs font-medium text-[#344054]
            no-underline transition hover:text-[#111827]
          "
          href="/productos/audifonos"
        >
          Audífonos
        </Link>
      </nav>

      <PublicSearchForm />

      <div className="col-start-2 row-start-1 flex flex-none items-center gap-3">
        <CartButton />
        <Button href="/login" size="wide">
          <FiUser aria-hidden="true" className="h-4 w-4" />
          Ingresar
        </Button>
      </div>
    </header>
  );
}
