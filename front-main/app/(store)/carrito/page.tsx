"use client";

import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent, type ReactNode } from "react";
import {
  FiAlertTriangle,
  FiLock,
  FiMinus,
  FiPlus,
  FiShoppingBag,
  FiTrash2,
} from "react-icons/fi";
import Breadcrumbs from "@/app/components/shared/breadcrumbs";
import Container from "@/app/components/shared/container";
import PublicFooter from "@/app/components/layout/public/public-footer";
import PublicHeader from "@/app/components/layout/public/public-header";
import Button from "@/app/components/ui/button";
import Snackbar, { type SnackbarType } from "@/app/components/ui/snackbar";
import { useCart } from "@/app/hooks/use-cart";
import { getApiErrorMessage } from "@/app/modules/api-error.service";
import { createOrder } from "@/app/modules/orders/order/services/order.service";
import { formatPrice } from "@/app/modules/orders/order/services/order-format";
import type { CartItem } from "@/app/modules/users/cart/types/cart.type";

const MIN_ADDRESS_LENGTH = 10;
const MAX_ADDRESS_LENGTH = 255;

function StorePageShell({ children }: { children: ReactNode }) {
  return (
    <Container>
      <PublicHeader />
      <main className="min-h-[60vh] px-4 pb-12 pt-6 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <Breadcrumbs items={[{ label: "Inicio", href: "/" }, { label: "Carrito" }]} />
        {children}
      </main>
      <PublicFooter />
    </Container>
  );
}

function CartLine({
  item,
  isPending,
  onQuantityChange,
  onRemove,
}: {
  item: CartItem;
  isPending: boolean;
  onQuantityChange: (quantity: number) => void;
  onRemove: () => void;
}) {
  const productHref = item.categorySlug
    ? `/productos/${item.categorySlug}/${item.productSlug}`
    : null;
  const maxQuantity = Math.min(item.availableStock, 99);
  const stockMessage = item.availableStock <= 0
    ? "Este producto se agotó. Quítalo para continuar."
    : `Solo quedan ${item.availableStock} unidades. Ajusta la cantidad para continuar.`;

  return (
    <li className={`
      grid grid-cols-[72px_minmax(0,1fr)] gap-4 p-4 sm:grid-cols-[96px_minmax(0,1fr)_auto]
      border-b border-[#edf1f6] last:border-b-0
      ${isPending ? "opacity-60" : ""}
    `}>
      <div className="relative aspect-square w-full overflow-hidden rounded-md bg-[#f1f4f8]">
        {item.imageUrl ? (
          <Image
            src={item.imageUrl}
            alt={item.productName}
            fill
            unoptimized
            sizes="96px"
            className="object-cover"
          />
        ) : (
          <FiShoppingBag aria-hidden="true" className="absolute inset-0 m-auto h-6 w-6 text-slate-400" />
        )}
      </div>

      <div className="flex min-w-0 flex-col gap-1">
        <span className="text-xs font-semibold uppercase tracking-wide text-[#647895]">
          {item.brandName ?? "Net Computer"}
        </span>
        {productHref ? (
          <Link
            className="
              line-clamp-2
              text-base font-semibold leading-snug text-[#111827]
              no-underline transition hover:text-[#344054]
            "
            href={productHref}
          >
            {item.productName}
          </Link>
        ) : (
          <span className="line-clamp-2 text-base font-semibold leading-snug text-[#111827]">
            {item.productName}
          </span>
        )}
        <span className="text-sm text-[#647895]">S/. {formatPrice(item.unitPrice)} c/u</span>
        {!item.available && (
          <span className="mt-1 inline-flex items-start gap-1.5 text-sm text-[#9a3412]">
            <FiAlertTriangle aria-hidden="true" className="mt-0.5 h-4 w-4 shrink-0" />
            {stockMessage}
          </span>
        )}
      </div>

      <div className="
        col-span-2 flex items-center justify-between gap-4
        sm:col-span-1 sm:flex-col sm:items-end sm:justify-between
      ">
        <div className="inline-flex h-9 items-center rounded-md border border-[#d5deea] bg-white">
          <button
            aria-label={`Reducir cantidad de ${item.productName}`}
            className="grid h-9 w-9 place-items-center text-[#526582] disabled:text-slate-300"
            disabled={isPending || item.quantity <= 1}
            onClick={() => onQuantityChange(item.quantity - 1)}
            type="button"
          >
            <FiMinus aria-hidden="true" />
          </button>
          <span
            className="min-w-8 text-center text-sm font-semibold text-[#111827]"
            aria-label={`Cantidad: ${item.quantity}`}
          >
            {item.quantity}
          </span>
          <button
            aria-label={`Aumentar cantidad de ${item.productName}`}
            className="grid h-9 w-9 place-items-center text-[#526582] disabled:text-slate-300"
            disabled={isPending || item.quantity >= maxQuantity}
            onClick={() => onQuantityChange(item.quantity + 1)}
            type="button"
          >
            <FiPlus aria-hidden="true" />
          </button>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-base font-bold text-[#111827]">
            S/. {formatPrice(item.subtotal)}
          </span>
          <button
            aria-label={`Quitar ${item.productName} del carrito`}
            className="
              inline-flex h-8 w-8 items-center justify-center
              text-red-500
              rounded transition hover:bg-red-50 hover:text-red-700 disabled:opacity-50
            "
            disabled={isPending}
            onClick={onRemove}
            type="button"
          >
            <FiTrash2 aria-hidden="true" className="h-4 w-4" />
          </button>
        </div>
      </div>
    </li>
  );
}

export default function CartPage() {
  const router = useRouter();
  const {
    cart,
    isLoading,
    isAuthenticated,
    pendingProductId,
    updateQuantity,
    removeItem,
    clear,
    refresh,
  } = useCart();
  const [shippingAddress, setShippingAddress] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [checkoutError, setCheckoutError] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState<{ type: SnackbarType; text: string } | null>(null);

  const runCartAction = async (action: () => Promise<void>) => {
    setSnackbar(null);
    try {
      await action();
    } catch (error) {
      setSnackbar({
        type: "error",
        text: error instanceof Error ? error.message : "No se pudo actualizar el carrito.",
      });
    }
  };

  const trimmedAddress = shippingAddress.trim();
  const isAddressValid = trimmedAddress.length >= MIN_ADDRESS_LENGTH;
  const isBusy = isSubmitting || pendingProductId !== null;

  const handleCheckout = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setCheckoutError(null);

    if (!isAddressValid) {
      setCheckoutError(`La dirección debe tener al menos ${MIN_ADDRESS_LENGTH} caracteres.`);
      return;
    }

    setIsSubmitting(true);
    try {
      const order = await createOrder({ shippingAddress: trimmedAddress });
      router.push(`/pedidos/${order.id}?nuevo=1`);
      void refresh();
    } catch (error) {
      setCheckoutError(getApiErrorMessage(error, "No se pudo confirmar tu pedido. Inténtalo de nuevo."));
      await refresh();
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <StorePageShell>
        <p className="m-0 text-base text-[#647895]">Cargando tu carrito...</p>
      </StorePageShell>
    );
  }

  if (cart.items.length === 0) {
    return (
      <StorePageShell>
        <div className="flex min-h-[45vh] flex-col items-center justify-center gap-4 text-center">
          <span className="grid h-14 w-14 place-items-center rounded-full bg-[#f1f4f8] text-[#526582]">
            <FiShoppingBag aria-hidden="true" className="h-6 w-6" />
          </span>
          <h1 className="m-0 text-2xl font-bold text-[#111827]">Tu carrito está vacío</h1>
          <p className="m-0 max-w-md text-base text-[#647895]">
            Explora nuestro catálogo y añade los productos que quieras comprar.
          </p>
          <div className="flex flex-wrap justify-center gap-3">
            <Button href="/" size="large">Ver productos</Button>
            {isAuthenticated && (
              <Button href="/pedidos" variant="outline" size="large">Mis pedidos</Button>
            )}
          </div>
        </div>
      </StorePageShell>
    );
  }

  return (
    <StorePageShell>
      {snackbar && (
        <Snackbar key={`${snackbar.type}-${snackbar.text}`} type={snackbar.type} text={snackbar.text} />
      )}

      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="m-0 text-3xl font-bold leading-tight text-[#111827]">Carrito de compras</h1>
          <p className="mb-0 mt-1 text-base text-[#647895]">
            {cart.itemCount === 1 ? "1 producto" : `${cart.itemCount} productos`}
          </p>
        </div>
        <Button
          type="button"
          variant="transparent"
          size="small"
          disabled={isBusy}
          onClick={() => void runCartAction(clear)}
        >
          <FiTrash2 aria-hidden="true" className="h-3.5 w-3.5" />
          Vaciar carrito
        </Button>
      </div>

      <div className="mt-5 grid grid-cols-1 items-start gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
        <ul className="m-0 list-none overflow-hidden p-0 rounded-lg border border-[#e0e6ef] bg-white">
          {cart.items.map((item) => (
            <CartLine
              item={item}
              isPending={pendingProductId === item.productId || isSubmitting}
              key={item.productId}
              onQuantityChange={(quantity) => void runCartAction(() => updateQuantity(item.productId, quantity))}
              onRemove={() => void runCartAction(() => removeItem(item.productId))}
            />
          ))}
        </ul>

        <aside className="p-5 rounded-lg border border-[#e0e6ef] bg-[#f8fafc] lg:sticky lg:top-4">
          <h2 className="m-0 text-lg font-bold text-[#111827]">Resumen del pedido</h2>
          <dl className="mb-0 mt-4 space-y-2 text-base">
            <div className="flex justify-between gap-3 text-[#526582]">
              <dt>Productos ({cart.itemCount})</dt>
              <dd className="m-0">S/. {formatPrice(cart.total)}</dd>
            </div>
            <div className="flex justify-between gap-3 text-[#526582]">
              <dt>Envío</dt>
              <dd className="m-0">Se coordina tras la compra</dd>
            </div>
            <div className="flex justify-between gap-3 border-t border-[#dce3ed] pt-3 text-lg font-bold text-[#111827]">
              <dt>Total</dt>
              <dd className="m-0">S/. {formatPrice(cart.total)}</dd>
            </div>
          </dl>

          {!cart.checkoutAvailable && (
            <p className="
              mb-0 mt-4 flex items-start gap-2 px-3 py-2
              text-sm text-[#9a3412]
              rounded-md border border-orange-200 bg-orange-50
            " role="alert">
              <FiAlertTriangle aria-hidden="true" className="mt-0.5 h-4 w-4 shrink-0" />
              Algunos productos no tienen stock suficiente. Ajusta tu carrito para continuar.
            </p>
          )}

          {isAuthenticated ? (
            <form className="mt-5" onSubmit={handleCheckout} noValidate>
              <label
                className="mb-1.5 block text-sm font-semibold text-[#344054]"
                htmlFor="shipping-address"
              >
                Dirección de envío
                <span className="ml-1 text-red-600" aria-hidden="true">*</span>
              </label>
              <textarea
                className="
                  min-h-[84px] w-full resize-y px-3 py-2
                  text-sm text-[#111827] placeholder:text-[#94a3b8]
                  rounded-md border border-[#d5deea] bg-white outline-none
                  transition focus:border-[#64748b] focus:ring-2 focus:ring-slate-100
                "
                id="shipping-address"
                maxLength={MAX_ADDRESS_LENGTH}
                placeholder="Av. Ejemplo 123, distrito, ciudad"
                required
                value={shippingAddress}
                onChange={(event) => setShippingAddress(event.currentTarget.value)}
                aria-describedby="shipping-address-help"
              />
              <p id="shipping-address-help" className="mb-0 mt-1 text-xs text-[#71839d]">
                {trimmedAddress.length}/{MAX_ADDRESS_LENGTH} caracteres
              </p>

              {checkoutError && (
                <p className="
                  mb-0 mt-3 px-3 py-2
                  text-sm text-red-700
                  rounded-md border border-red-200 bg-red-50
                " role="alert">
                  {checkoutError}
                </p>
              )}

              <div className="mt-4 grid">
                <Button
                  type="submit"
                  size="large"
                  disabled={isBusy || !cart.checkoutAvailable || !isAddressValid}
                >
                  <FiLock aria-hidden="true" className="h-4 w-4" />
                  {isSubmitting ? "Confirmando..." : "Confirmar pedido"}
                </Button>
              </div>
            </form>
          ) : (
            <div className="mt-5 grid gap-3">
              <p className="m-0 text-sm text-[#526582]">
                Inicia sesión para confirmar tu pedido. Guardaremos los productos de tu carrito.
              </p>
              <Button href="/login?redirect=/carrito" size="large">
                <FiLock aria-hidden="true" className="h-4 w-4" />
                Iniciar sesión para comprar
              </Button>
            </div>
          )}

          <div className="mt-3 grid">
            <Button href="/" variant="outline" size="large">Seguir comprando</Button>
          </div>
        </aside>
      </div>
    </StorePageShell>
  );
}
